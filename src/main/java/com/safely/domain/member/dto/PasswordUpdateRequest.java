package com.safely.domain.member.dto;

public record PasswordUpdateRequest(String currentPassword, String newPassword) {}