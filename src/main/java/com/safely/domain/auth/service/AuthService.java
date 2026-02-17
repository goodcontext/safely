package com.safely.domain.auth.service;

import com.safely.domain.auth.dto.*;
import com.safely.domain.auth.entity.CustomUserDetails;
import com.safely.global.exception.BusinessException;
import com.safely.global.exception.ErrorCode;
import com.safely.global.exception.auth.EmailDuplicateException;
import com.safely.global.exception.auth.InvalidTokenException;
import com.safely.global.exception.auth.RefreshTokenNotFoundException;
import com.safely.global.exception.common.EntityNotFoundException;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final PasswordEncoder passwordEncoder;

    public TokenResponse login(LoginRequest request) {
        log.info("[*] 로그인 시도: Email={}", request.email());

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.email(),
                                request.password()
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

        log.info("[+] 로그인 성공 및 토큰 발급 완료: MemberID={}", member.getId());
        return new TokenResponse(accessToken, refreshToken);
    }

    public SignupResponse signup(SignupRequest request) {

        // 이메일 중복 체크
        if (memberRepository.existsByEmail(request.email())) {
            log.warn("[!] 회원가입 실패: 이미 존재하는 이메일입니다. Email: {}", request.email());
            throw new EmailDuplicateException();
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        // 저장
        Member member = Member.builder()
                .email(request.email())
                .password(encodedPassword)
                .name(request.name())
                .profileImage(null)  // 프로필 이미지 적용 시 변경
                .authority("ROLE_USER")
                .build();

        Member saved = null;

        try {
            saved = memberRepository.save(member);
            log.info("[+] 회원가입 성공: MemberID={}, Email={}", saved.getId(), saved.getEmail());
        } catch (Exception e) {
            log.error("[-] 회원가입 중 DB 저장 실패. Email={}, Cause={}", request.email(), e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return SignupResponse.from(saved);
    }

    public TokenResponse reIssue(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        if (!jwtProvider.validateToken(refreshToken)) {
            log.warn("[!] 토큰 재발급 실패: 유효하지 않은 리프레시 토큰");
            throw new InvalidTokenException();
        }

        Long userId =
                Long.valueOf(jwtProvider.getSubject(refreshToken));
        String key = "refresh:" + userId;

        String storedRefreshToken =
                redisTemplate.opsForValue().get(key);

        if (storedRefreshToken == null
                || !storedRefreshToken.equals(refreshToken)) {
            log.warn("[!] 토큰 재발급 실패: Redis 내 토큰 불일치. MemberID={}", userId);
            throw new RefreshTokenNotFoundException();
        }

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("[-] 토큰 재발급 중 사용자 조회 실패. MemberID={}", userId);
                    return new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND);
                });

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

        log.info("[*] 토큰 재발급 완료: MemberID={}", member.getId());
        return new TokenResponse(
                newAccessToken,
                newRefreshToken
        );
    }

    public void logout(String accessToken) {

        if (!jwtProvider.validateToken(accessToken)) {
            log.warn("[!] 로그아웃 실패: 유효하지 않은 액세스 토큰 요청");
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

        log.info("[-] 로그아웃 처리 완료 (Redis 블랙리스트 등록): MemberID={}", userId);
    }
}