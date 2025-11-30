package com.safely.domain.auth.service;

import com.safely.domain.auth.dto.LoginRequest;
import com.safely.domain.auth.dto.SignupRequest;
import com.safely.domain.auth.dto.TokenResponse;
import com.safely.domain.auth.entity.CustomUserDetails;
import com.safely.domain.member.entity.Member;
import com.safely.domain.member.repository.MemberRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

// ★ 핵심 수정 2가지:
// 1. spring.data.redis.repositories.enabled=false (Redis Repository 끄기)
// 2. spring.jpa.defer-datasource-initialization=true (테이블 생성 후 data.sql 실행하기)
@SpringBootTest(properties = {
        "spring.data.redis.repositories.enabled=false",
        "spring.jpa.defer-datasource-initialization=true"
})
@Transactional
class AuthServiceIntegrationTest {

    @Autowired AuthService authService;
    @Autowired MemberRepository memberRepository;
    @Autowired PasswordEncoder passwordEncoder;

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
        SignupRequest request = SignupRequest.builder()
                .email("newtest@safely.com") // data.sql과 겹치지 않게 이메일 변경
                .password("rawPassword")
                .name("테스터")
                .build();

        authService.signup(request);

        Member saved = memberRepository.findByEmail("newtest@safely.com").orElseThrow();
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

        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();
    }

    @Test
    @DisplayName("회원가입 실패: 이미 존재하는 이메일")
    void signup_Fail_DuplicateEmail() {
        authService.signup(new SignupRequest("fail_test@safely.com", "1234", "먼저가입"));
        SignupRequest newRequest = new SignupRequest("fail_test@safely.com", "5678", "중복가입시도");

        assertThatThrownBy(() -> authService.signup(newRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");
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
}