package com.safely.domain.auth.dto;

public record LoginResponse(String accessToken, String refreshToken) {}