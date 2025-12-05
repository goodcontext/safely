package com.safely.domain.group.controller;

import com.safely.domain.auth.entity.CustomUserDetails;
import com.safely.domain.group.dto.*;
import com.safely.domain.group.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    // 1. 그룹 생성
    @PostMapping
    public ResponseEntity<Long> createGroup(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody GroupCreateRequest request) {
        Long groupId = groupService.createGroup(user.getMember().getId(), request);
        return ResponseEntity.ok(groupId);
    }

    // 2. 내 그룹 목록 조회
    @GetMapping
    public ResponseEntity<List<GroupResponse>> getMyGroups(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(groupService.getMyGroups(user.getMember().getId()));
    }

    // 3. 그룹 상세 조회
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDetailResponse> getGroupDetail(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getGroupDetail(groupId, user.getMember().getId()));
    }

    // 4. 그룹 정보 수정
    @PatchMapping("/{groupId}")
    public ResponseEntity<Void> updateGroup(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long groupId,
            @RequestBody GroupUpdateRequest request) {
        groupService.updateGroup(groupId, user.getMember().getId(), request);
        return ResponseEntity.ok().build();
    }

    // 5. 그룹 삭제
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long groupId) {
        groupService.deleteGroup(groupId, user.getMember().getId());
        return ResponseEntity.ok().build();
    }

    // 6. 초대 코드로 그룹 가입 (멤버 추가)
    @PostMapping("/join")
    public ResponseEntity<Void> joinGroup(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam String inviteCode) {
        groupService.joinGroupByCode(user.getMember().getId(), inviteCode);
        return ResponseEntity.ok().build();
    }
}