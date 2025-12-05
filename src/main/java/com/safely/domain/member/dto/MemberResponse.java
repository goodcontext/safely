package com.safely.domain.member.dto;

import com.safely.domain.member.entity.Member;

public record MemberResponse(Long id, String email, String name, String profileImage) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getEmail(), member.getName(), member.getProfileImage());
    }
}