package com.safely.domain.member.controller;

import com.safely.domain.auth.entity.CustomUserDetails;
import com.safely.domain.member.dto.*;
import com.safely.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    // 1. 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMyInfo(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(memberService.getMyInfo(user.getMember().getId()));
    }

    // 2. 통합 정보 수정 (이름 + 사진 + 비밀번호)
    // Multipart 요청을 받기 위해 consumes 설정 추가
    // JSON 형식으로 이미지 보내려면 BASE64로 인코딩해야 하는데, 비효율적임. 그래서 consumes 구문을 넣음.
    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateMember(
            @AuthenticationPrincipal CustomUserDetails user,
            // JSON 데이터는 request 파트로 (선택적)
            @RequestPart(value = "request", required = false) MemberUpdateRequest request,
            // 파일 데이터는 file 파트로 (선택적)
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        memberService.updateMember(user.getMember().getId(), request, file);
        return ResponseEntity.ok().build();
    }

    // 3. 회원 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal CustomUserDetails user) {
        memberService.deleteMember(user.getMember().getId());
        return ResponseEntity.noContent().build();
    }
}