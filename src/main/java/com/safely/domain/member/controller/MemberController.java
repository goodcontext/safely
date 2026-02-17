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

    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMyInfo(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(memberService.getMyInfo(user.getMember().getId()));
    }

    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateMember(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestPart(value = "request", required = false) MemberUpdateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        memberService.updateMember(user.getMember().getId(), request, file);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal CustomUserDetails user) {
        memberService.deleteMember(user.getMember().getId());
        return ResponseEntity.noContent().build();
    }
}