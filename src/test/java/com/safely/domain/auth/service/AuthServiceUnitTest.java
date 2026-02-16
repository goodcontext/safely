package com.safely.domain.auth.service;

import com.safely.domain.auth.dto.*;
import com.safely.domain.auth.entity.CustomUserDetails;
import com.safely.domain.member.entity.Member;
import com.safely.domain.member.repository.MemberRepository;
import com.safely.global.security.jwt.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTest {
    @InjectMocks
    AuthService authService;

    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    MemberRepository memberRepository;
    @Mock
    JwtProvider jwtProvider;
    @Mock
    RedisTemplate<String, String> redisTemplate;
    @Mock
    PasswordEncoder passwordEncoder;

    // Redis의 opsForValue()를 Mocking 하기 위해 필요
    @Mock
    ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("회원가입 성공: 비밀번호가 암호화되고 회원이 저장된다.")
    void signup_Success() {
        // Given
        SignupRequest request = new SignupRequest("test@email.com", "1234", "테스터");

        // Mock: 이메일 중복 아님
        given(memberRepository.existsByEmail(request.email())).willReturn(false);
        // Mock: 비번 암호화
        given(passwordEncoder.encode(request.password())).willReturn("encodedPw");
        // Mock: 저장 후 반환할 객체
        Member savedMember = Member.builder()
                .id(1L)
                .email(request.email())
                .name(request.name())
                .build();
        given(memberRepository.save(any(Member.class))).willReturn(savedMember);

        // When
        SignupResponse response = authService.signup(request);

        // Then
        assertThat(response.email()).isEqualTo(request.email());
        assertThat(response.name()).isEqualTo(request.name());

        // Verify: save 메서드가 1번 호출되었는지 확인
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("회원가입 실패: 이미 존재하는 이메일이면 예외 발생")
    void signup_Fail_DuplicateEmail() {
        // Given
        SignupRequest request = new SignupRequest("duplicate@email.com", "1234", "중복자");

        // Mock: 이메일 중복임 (true)
        given(memberRepository.existsByEmail(request.email())).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");

        // Verify: 저장은 호출되면 안 됨
        verify(memberRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("로그인 성공: 토큰이 발급되고 Refresh 토큰이 Redis에 저장된다.")
    void login_Success() {
        // Given
        LoginRequest request = new LoginRequest("test@email.com", "1234");
        Member member = Member.builder().id(1L).email("test@email.com").authority("ROLE_USER").build();

        // 1. AuthenticationManager Mocking
        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(userDetails.getMember()).willReturn(member);

        // 2. JwtProvider Mocking
        given(jwtProvider.generateAccessToken(any(), any(), any())).willReturn("access-token");
        given(jwtProvider.generateRefreshToken(any())).willReturn("refresh-token");
        given(jwtProvider.getRefreshTokenExpireMs()).willReturn(10000L);

        // 3. Redis Mocking
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // When
        TokenResponse response = authService.login(request);

        // Then
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");

        // Verify: Redis에 Refresh 토큰 저장 로직이 호출되었는지 확인
        // eq는 set명령의 결괏값과 비교하는 것이 아니고, 인숫값으로 value값들을(10000L 같은 값들) 받았는지 비교함.
        verify(valueOperations).set(
                eq("refresh:" + member.getId()),
                eq("refresh-token"),
                eq(10000L),
                eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    @DisplayName("토큰 재발급 성공: Redis 토큰과 일치하면 재발급")
    void reIssue_Success() {
        // Given
        String oldRefreshToken = "old-refresh-token";
        Long userId = 1L;

        RefreshTokenRequest request = new RefreshTokenRequest(oldRefreshToken);

        // Mocking
        given(jwtProvider.validateToken(oldRefreshToken)).willReturn(true);
        given(jwtProvider.getSubject(oldRefreshToken)).willReturn(String.valueOf(userId));

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("refresh:" + userId)).willReturn(oldRefreshToken); // Redis에 저장된 값과 일치

        Member member = Member.builder().id(userId).email("test@a.com").authority("ROLE_USER").build();
        given(memberRepository.findById(userId)).willReturn(Optional.of(member));

        given(jwtProvider.generateAccessToken(any(), any(), any())).willReturn("new-access");
        given(jwtProvider.generateRefreshToken(any())).willReturn("new-refresh");

        // When
        TokenResponse response = authService.reIssue(request);

        // Then
        assertThat(response.accessToken()).isEqualTo("new-access");
        assertThat(response.refreshToken()).isEqualTo("new-refresh");

        // Verify: Redis 업데이트 확인
        verify(valueOperations).set(eq("refresh:" + userId), eq("new-refresh"), anyLong(), any());
    }

    @Test
    @DisplayName("로그아웃: Access 토큰 블랙리스트 처리 및 Refresh 토큰 삭제")
    void logout_Success() {
        // Given
        String accessToken = "valid-access-token";
        Long userId = 1L;
        long remainMs = 5000L;

        given(jwtProvider.validateToken(accessToken)).willReturn(true);
        given(jwtProvider.getSubject(accessToken)).willReturn(String.valueOf(userId));
        given(jwtProvider.getRemainExpiration(accessToken)).willReturn(remainMs);

        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // When
        authService.logout(accessToken);

        // Then
        // 1. Access Token 블랙리스트 추가 확인
        verify(valueOperations).set(
                eq("blacklist:access:" + accessToken),
                eq("logout"),
                eq(remainMs),
                eq(TimeUnit.MILLISECONDS)
        );

        // 2. Refresh Token 삭제 확인
        verify(redisTemplate).delete("refresh:" + userId); // delete는 아까 삭제 했었니? 하고 질의하는 것.
    }
}