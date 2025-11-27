package com.safely.domain.auth.service;

import com.safely.domain.auth.dto.*;
import com.safely.domain.auth.entity.CustomUserDetails;
import com.safely.global.security.jwt.JwtProvider;
import com.safely.domain.member.entity.Member;
import com.safely.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final PasswordEncoder passwordEncoder;

    public TokenResponse login(LoginRequest request) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();
        Member member = userDetails.getMember();

        String accessToken =
                jwtProvider.generateAccessToken(
                        member.getId(),
                        member.getEmail(),
                        member.getAuthority()
                );
        String refreshToken =
                jwtProvider.generateRefreshToken(
                        member.getId()
                );

        String key = "refresh:" + member.getId();
        long expireMs = jwtProvider.getRefreshTokenExpireMs();

        redisTemplate.opsForValue()
                .set(key, refreshToken, expireMs,
                        TimeUnit.MILLISECONDS);

        return new TokenResponse(accessToken, refreshToken);
    }

    public SignupResponse signup(SignupRequest request) {

        // 이메일 중복 체크
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 저장
        Member member = Member.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .profileImage(null)  // 프로필 이미지 적용 시 변경
                .authority("ROLE_USER")
                .build();

        Member saved = null;

        try {
            saved = memberRepository.save(member);
        } catch (Exception e) {
            throw new RuntimeException("회원 저장중 오류가 발생했습니다.", e);
        }

        return SignupResponse.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .name(saved.getName())
                .build();
    }

    public TokenResponse reIssue(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtProvider.validateToken(refreshToken)) {
            throw new RuntimeException("잘못된 리프레시 토큰입니다.");
        }

        Long userId =
                Long.valueOf(jwtProvider.getSubject(refreshToken));
        String key = "refresh:" + userId;

        String storedRefreshToken =
                redisTemplate.opsForValue().get(key);

        if (storedRefreshToken == null
                || !storedRefreshToken.equals(refreshToken)) {
            throw new RuntimeException(
                    "리프레시 토큰이 만료되었거나 일치하지 않습니다."
            );
        }

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "사용자를 찾을 수 없습니다."
                ));

        String newAccessToken =
                jwtProvider.generateAccessToken(
                        member.getId(),
                        member.getEmail(),
                        member.getAuthority()
                );

        // 새로운 refresh 토큰 발급
        String newRefreshToken =
                jwtProvider.generateRefreshToken(
                        member.getId()
                );
        long expireMs =
                jwtProvider.getRefreshTokenExpireMs();

        redisTemplate.opsForValue()
                .set(key, newRefreshToken, expireMs,
                        TimeUnit.MILLISECONDS);

        return new TokenResponse(
                newAccessToken,
                newRefreshToken
        );
    }

    public void logout(String accessToken) {

        if (!jwtProvider.validateToken(accessToken)) {
            return;
        }

        Long userId =
                Long.valueOf(jwtProvider.getSubject(accessToken));

        long remainMs =
                jwtProvider.getRemainExpiration(accessToken);

        // access 토큰 블랙리스트 처리
        redisTemplate.opsForValue().set(
                "blacklist:access:" + accessToken,
                "logout",
                remainMs,
                TimeUnit.MILLISECONDS
        );

        // refresh 토큰 삭제
        String key = "refresh:" + userId;
        redisTemplate.delete(key);
    }
}