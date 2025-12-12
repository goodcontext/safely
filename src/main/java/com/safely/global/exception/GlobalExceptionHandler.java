package com.safely.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 1. [404 NOT_FOUND] 리소스 조회 실패 (존재하지 않는 ID)
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFoundException(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Not Found: 요청하신 리소스를 찾을 수 없습니다.");
    }

    // 2. [400 BAD_REQUEST] 비즈니스 로직 오류 (IllegalArgumentException)
    // 예: "이미 사용 중인 이메일입니다.", "그룹 관리자만 수행 가능합니다."
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Bad Request: " + ex.getMessage());
    }

    // 3. [400 BAD_REQUEST] DTO 유효성 검증 오류 (MethodArgumentNotValidException)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException ex) {
        String defaultMessage = ex.getBindingResult().getFieldError().getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Validation Failed: " + defaultMessage);
    }

    // 4. [400 BAD_REQUEST] 파라미터 타입 불일치 오류 (Type Mismatch)
    // 예: PathVariable에 Long이 와야 하는데 문자가 온 경우
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid Type: '%s'는 유효한 타입이 아닙니다. (%s)", ex.getValue(), ex.getRequiredType().getSimpleName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(message);
    }

    // 5. [403 FORBIDDEN] 권한 부족 오류 (Security AccessDeniedException)
    // JWT는 유효하지만, 리소스 접근 권한이 없을 때 (예: 그룹 멤버가 아닌데 그룹 상세 정보 요청 시)
    // @AuthenticationPrincipal로 유저 정보는 가져왔으나, @PreAuthorize 등에서 막힐 때 발생합니다.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Forbidden: 해당 리소스에 접근할 권한이 없습니다.");
    }

    // 6. [413 PAYLOAD_TOO_LARGE] 파일 크기 초과 오류 (S3 업로드 시)
    // 파일 업로드 크기(10MB)를 초과할 때 발생합니다.
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body("Payload Too Large: 파일 크기가 너무 큽니다. (최대 10MB)");
    }

    // 7. [500 INTERNAL_SERVER_ERROR] 예상치 못한 모든 심각한 오류
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        // 실제로는 이 곳에 로깅 시스템(Sentry, ELK 등)을 연동하여 알림을 받아야 합니다.
        // ex.printStackTrace(); // 운영 환경에서는 로그 파일에만 기록해야 합니다.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal Server Error: 서버 처리 중 예상치 못한 오류가 발생했습니다.");
    }
}