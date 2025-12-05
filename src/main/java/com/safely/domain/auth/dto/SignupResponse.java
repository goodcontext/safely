package com.safely.domain.auth.dto;

import com.safely.domain.member.entity.Member;

public record SignupResponse(Long id, String email, String name) {
    public static SignupResponse from(Member member) {
        return new SignupResponse(member.getId(), member.getEmail(), member.getName());
    }
}