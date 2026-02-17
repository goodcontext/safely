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

    // 내 정보 조회
    public MemberResponse getMyInfo(Long memberId) {
        Member member = findMemberById(memberId);
        return MemberResponse.from(member);
    }

    // [통합 수정] 이름, 프로필 사진, 비밀번호를 한 번에 처리
    @Transactional
    public void updateMember(Long memberId, MemberUpdateRequest request, MultipartFile file) {
        Member member = findMemberById(memberId);
        boolean isUpdated = false;

        // 1. 이름 변경 (값이 있을 때만)
        if (request != null && hasText(request.name())) {
            member.updateProfile(request.name(), null);
            isUpdated = true;
        }

        // 2. 프로필 사진 변경 (파일이 있을 때만)
        if (file != null && !file.isEmpty()) {
            // 기존 이미지가 있다면 S3에서 삭제
            if (member.getProfileImage() != null) {
                s3Service.deleteObject(member.getProfileImage());
            }
            // 새 이미지 업로드 및 적용
            String imageUrl = s3Service.upload(file, "profile");
            member.updateProfile(null, imageUrl);
            isUpdated = true;
        }

        // 3. 비밀번호 변경 (새 비밀번호 값이 있을 때만)
        if (request != null && hasText(request.newPassword())) {
            // 현재 비밀번호 입력 확인
            if (!hasText(request.currentPassword())) {
                log.warn("[!] 비밀번호 변경 실패: 현재 비밀번호 미입력. MemberID={}", memberId);
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            // 현재 비밀번호 일치 확인
            if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
                log.warn("[!] 비밀번호 변경 실패: 현재 비밀번호 불일치. MemberID={}", memberId);
                throw new PasswordMismatchException();
            }
            // 비밀번호 변경 적용
            member.updatePassword(passwordEncoder.encode(request.newPassword()));
            log.info("[*] 비밀번호 변경 완료: MemberID={}", memberId);
            isUpdated = true;
        }

        if (isUpdated) {
            log.info("[*] 회원 정보 수정 완료: MemberID={}", memberId);
        }
    }

    // 회원 탈퇴
    @Transactional
    public void deleteMember(Long memberId) {
        Member member = findMemberById(memberId);

        // S3 프로필 이미지 삭제
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

    // 문자열 유효성 검사 헬퍼
    private boolean hasText(String text) {
        return text != null && !text.isBlank();
    }
}