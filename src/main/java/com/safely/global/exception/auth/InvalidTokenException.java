package com.safely.global.exception.auth;

import com.safely.global.exception.BusinessException;
import com.safely.global.exception.ErrorCode;

public class InvalidTokenException extends BusinessException {
    public InvalidTokenException() {
        super(ErrorCode.INVALID_TOKEN);
    }
}