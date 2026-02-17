package com.safely.domain.auth.service;

import com.safely.global.exception.ErrorCode;
import com.safely.global.exception.common.EntityNotFoundException;
import com.safely.domain.auth.entity.CustomUserDetails;
import com.safely.domain.member.entity.Member;
import com.safely.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("[!] 로그인 실패: 존재하지 않는 유저 Email={}", email);
                    return new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND);
                });

        log.info("[*] 로그인 유저 정보 로드 성공: Email={}", member.getEmail());

        return new CustomUserDetails(member);
    }
}
