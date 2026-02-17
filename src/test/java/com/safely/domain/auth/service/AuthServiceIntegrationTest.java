package com.safely.domain.auth.service;

import com.safely.domain.auth.dto.LoginRequest;
import com.safely.domain.auth.dto.RefreshTokenRequest;
import com.safely.domain.auth.dto.SignupRequest;
import com.safely.domain.auth.dto.TokenResponse;
import com.safely.domain.auth.entity.CustomUserDetails;
import com.safely.domain.member.entity.Member;
import com.safely.domain.member.repository.MemberRepository;
import com.safely.global.exception.auth.EmailDuplicateException;
import com.safely.global.exception.auth.InvalidTokenException;
import com.safely.global.exception.auth.RefreshTokenNotFoundException;
import com.safely.global.exception.common.EntityNotFoundException;
import com.safely.global.security.jwt.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
class AuthServiceIntegrationTest {

    @Autowired AuthService authService;
    @Autowired MemberRepository memberRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired
    JwtProvider jwtProvider;

    @MockitoBean RedisConnectionFactory redisConnectionFactory;
    @MockitoBean RedisTemplate<String, String> redisTemplate;
    @MockitoBean ValueOperations<String, String> valueOperations;
    @MockitoBean AuthenticationManager authenticationManager;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    @Test
    @DisplayName("회원가입 성공: 암호화 저장 확인")
    void signup_Success() {
        SignupRequest request = new SignupRequest("newtest@safely.com", "rawPassword", "테스터");

        authService.signup(request);

        Member saved = memberRepository.findByEmail("newtest@safely.com").orElseThrow(EntityNotFoundException::new);
        assertThat(saved.getPassword()).isNotEqualTo("rawPassword");
        assertThat(saved.getPassword()).startsWith("$2a$");
    }

    @Test
    @DisplayName("로그인 성공: 토큰 발급 확인")
    void login_Success() {
        SignupRequest signup = new SignupRequest("loginuser@safely.com", "1234", "유저");
        authService.signup(signup);

        Authentication auth = mock(Authentication.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        Member member = memberRepository.findByEmail("loginuser@safely.com").get();

        given(userDetails.getMember()).willReturn(member);
        given(auth.getPrincipal()).willReturn(userDetails);
        given(authenticationManager.authenticate(any())).willReturn(auth);

        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        TokenResponse response = authService.login(new LoginRequest("loginuser@safely.com", "1234"));

        assertThat(response.accessToken()).isNotNull();
        assertThat(response.refreshToken()).isNotNull();
    }

    @Test
    @DisplayName("회원가입 실패: 이미 존재하는 이메일")
    void signup_Fail_DuplicateEmail() {
        authService.signup(new SignupRequest("fail_test@safely.com", "1234", "먼저가입"));
        SignupRequest newRequest = new SignupRequest("fail_test@safely.com", "5678", "중복가입시도");

        assertThatThrownBy(() -> authService.signup(newRequest))
                .isInstanceOf(EmailDuplicateException.class);
    }

    @Test
    @DisplayName("로그인 실패: 비밀번호 불일치")
    void login_Fail_WrongPassword() {
        authService.signup(new SignupRequest("fail_test@safely.com", "1234", "유저"));

        given(authenticationManager.authenticate(any()))
                .willThrow(new BadCredentialsException("자격 증명 실패"));

        LoginRequest wrongRequest = new LoginRequest("fail_test@safely.com", "WRONG_PW");

        assertThatThrownBy(() -> authService.login(wrongRequest))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("토큰 재발급 성공: RefreshToken이 유효하고 Redis와 일치하면 성공")
    void reIssue_Success() {
        // 회원 저장
        Member member = memberRepository.save(Member.builder()
                .email("reissue@safely.com")
                .password("1234")
                .name("재발급유저")
                .authority("ROLE_USER")
                .build());

        // 유효한 Refresh Token 생성 (JwtProvider 사용)
        String refreshToken = jwtProvider.generateRefreshToken(member.getId());

        // Redis Mocking (Redis에 해당 토큰이 있다고 가정)
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("refresh:" + member.getId())).willReturn(refreshToken);

        // 실행
        TokenResponse response = authService.reIssue(new RefreshTokenRequest(refreshToken));

        // 검증
        assertThat(response.accessToken()).isNotNull();
        assertThat(response.refreshToken()).isNotNull();
        // Redis에 새 토큰이 저장되었는지 확인
        verify(valueOperations).set(eq("refresh:" + member.getId()), anyString(), anyLong(), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("토큰 재발급 실패: Redis에 저장된 토큰이 없거나 만료됨")
    void reIssue_Fail_NotFoundInRedis() {
        // 회원 저장 및 토큰 생성
        Member member = memberRepository.save(Member.builder().email("fail@safely.com").password("1234").name("실패유저").authority("ROLE_USER").build());
        String refreshToken = jwtProvider.generateRefreshToken(member.getId());

        // Redis Mocking (Redis에서 null 반환 = 만료됨)
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("refresh:" + member.getId())).willReturn(null);

        // 검증
        assertThatThrownBy(() -> authService.reIssue(new RefreshTokenRequest(refreshToken)))
                .isInstanceOf(RefreshTokenNotFoundException.class);
    }
}