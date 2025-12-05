package com.safely.domain.auth.dto;

import lombok.Builder;

@Builder
public record SignupRequest(String email, String password, String name) {}