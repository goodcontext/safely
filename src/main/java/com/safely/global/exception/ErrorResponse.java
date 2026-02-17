package com.safely.global.exception;

import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String code,
        String message
) {
    // 생성자 대신 사용할 정적 팩토리 메서드
    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(
                        LocalDateTime.now(),
                        errorCode.getStatus().value(),
                        errorCode.getStatus().name(),
                        errorCode.getCode(),
                        errorCode.getMessage()
                ));
    }

    // 메시지를 커스텀해야 하는 경우 (Validation 등)
    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode, String customMessage) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(
                        LocalDateTime.now(),
                        errorCode.getStatus().value(),
                        errorCode.getStatus().name(),
                        errorCode.getCode(),
                        customMessage
                ));
    }
}
