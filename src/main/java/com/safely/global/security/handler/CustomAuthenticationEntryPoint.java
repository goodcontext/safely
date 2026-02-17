package com.safely.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safely.global.exception.ErrorCode;
import com.safely.global.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        log.error("인증 실패 (401) - URL: {}, Message: {}", request.getRequestURI(), authException.getMessage());

        // ErrorCode에서 적절한 에러 가져오기 (INVALID_TOKEN 등 상황에 맞게 매핑 가능하지만, 여기선 공통 인증 에러로 처리)
        ErrorCode errorCode = ErrorCode.INVALID_TOKEN;

        // JSON 응답 생성
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                errorCode.getStatus().value(),
                errorCode.getStatus().name(),
                errorCode.getCode(),
                "인증이 필요하거나 유효하지 않은 토큰입니다."
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Java 객체 -> JSON 문자열 변환 후 출력
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}