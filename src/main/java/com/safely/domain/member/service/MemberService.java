package com.safely.domain.member.service;

import com.safely.domain.member.dto.*;
import com.safely.domain.member.entity.Member;
import com.safely.domain.member.repository.MemberRepository;
import com.safely.global.exception.BusinessException;
import com.safely.global.exception.ErrorCode;
import com.safely.global.exception.auth.PasswordMismatchException;
import com.safely.global.exception.common.EntityNotFoundException;
import com.safely.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final S3Service s3Service;
    private final PasswordEncoder passwordEncoder;

    public MemberResponse getMyInfo(Long memberId) {
        Member member = findMemberById(memberId);
        return MemberResponse.from(member);
    }

    @Transactional
    public void updateMember(Long memberId, MemberUpdateRequest request, MultipartFile file) {
        Member member = findMemberById(memberId);
        boolean isUpdated = false;

        if (request != null && hasText(request.name())) {
            member.updateProfile(request.name(), null);
            isUpdated = true;
        }

        if (file != null && !file.isEmpty()) {
            if (member.getProfileImage() != null) {
                s3Service.deleteObject(member.getProfileImage());
            }
            String imageUrl = s3Service.upload(file, "profile");
            member.updateProfile(null, imageUrl);
            isUpdated = true;
        }

        if (request != null && hasText(request.newPassword())) {
            if (!hasText(request.currentPassword())) {
                log.warn("[!] 비밀번호 변경 실패: 현재 비밀번호 미입력. MemberID={}", memberId);
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
                log.warn("[!] 비밀번호 변경 실패: 현재 비밀번호 불일치. MemberID={}", memberId);
                throw new PasswordMismatchException();
            }
            member.updatePassword(passwordEncoder.encode(request.newPassword()));
            log.info("[*] 비밀번호 변경 완료: MemberID={}", memberId);
            isUpdated = true;
        }

        if (isUpdated) {
            log.info("[*] 회원 정보 수정 완료: MemberID={}", memberId);
        }
    }

    @Transactional
    public void deleteMember(Long memberId) {
        Member member = findMemberById(memberId);

        if (member.getProfileImage() != null) {
            s3Service.deleteObject(member.getProfileImage());
        }

        memberRepository.delete(member);
        log.info("[-] 회원 탈퇴 및 삭제 완료: MemberID={}", memberId);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private boolean hasText(String text) {
        return text != null && !text.isBlank();
    }
}