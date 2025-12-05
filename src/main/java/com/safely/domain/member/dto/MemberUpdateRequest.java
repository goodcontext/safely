package com.safely.domain.member.dto;

public record MemberUpdateRequest(
        String name,
        String currentPassword, // 보안 검증용 (비밀번호 바꿀 때만 필수)
        String newPassword      // 바꿀 새 비밀번호
) {}