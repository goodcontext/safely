package com.safely.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common (공통)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "지원하지 않는 HTTP 메서드입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "서버 내부 오류가 발생했습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C005", "입력 값의 타입이 올바르지 않습니다."),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C006", "접근 권한이 없습니다."),

    // Auth (인증/회원)
    EMAIL_DUPLICATION(HttpStatus.CONFLICT, "A001", "이미 존재하는 이메일입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "A002", "이메일 또는 비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "유효하지 않거나 만료된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "A004", "리프레시 토큰을 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "A005", "존재하지 않는 회원입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "A006", "기존 비밀번호가 일치하지 않습니다."),

    // Group (그룹)
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "G001", "존재하지 않는 그룹입니다."),
    ALREADY_JOINED_GROUP(HttpStatus.CONFLICT, "G002", "이미 가입된 그룹입니다."),
    INVALID_INVITE_CODE(HttpStatus.BAD_REQUEST, "G003", "유효하지 않은 초대 코드입니다."),
    GROUP_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "G004", "그룹 관리자만 수행할 수 있는 작업입니다."),
    NOT_GROUP_MEMBER(HttpStatus.FORBIDDEN, "G005", "해당 그룹의 멤버가 아닙니다."),

    // Expense & Settlement (지출 & 정산)
    EXPENSE_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "존재하지 않는 지출 내역입니다."),
    SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "정산 내역을 찾을 수 없습니다."),
    CONCURRENCY_CONFLICT(HttpStatus.CONFLICT, "S002", "다른 사용자가 데이터를 수정 중입니다. 잠시 후 다시 시도해주세요."),

    // File (파일 업로드)
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F001", "파일 업로드에 실패했습니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "F002", "지원하지 않는 파일 형식입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
