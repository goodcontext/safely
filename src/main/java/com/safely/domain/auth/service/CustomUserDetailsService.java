package com.safely.domain.auth.service;

import com.safely.global.exception.NotFoundException;
import com.safely.domain.auth.entity.CustomUserDetails;
import com.safely.domain.member.entity.Member;
import com.safely.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(NotFoundException::new);

        log.info("[*] Username found : " + member.getEmail());

        return new CustomUserDetails(member);
    }
}
