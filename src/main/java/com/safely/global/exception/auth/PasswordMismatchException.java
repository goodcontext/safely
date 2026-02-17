package com.safely.global.exception.auth;

import com.safely.global.exception.BusinessException;
import com.safely.global.exception.ErrorCode;

public class PasswordMismatchException extends BusinessException {
    public PasswordMismatchException() {
        super(ErrorCode.PASSWORD_MISMATCH);
    }
}