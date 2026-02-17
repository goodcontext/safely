package com.safely.global.exception.auth;

import com.safely.global.exception.BusinessException;
import com.safely.global.exception.ErrorCode;

public class RefreshTokenNotFoundException extends BusinessException {
    public RefreshTokenNotFoundException() {
        super(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }
}