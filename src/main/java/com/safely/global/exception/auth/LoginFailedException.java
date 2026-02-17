package com.safely.global.exception.auth;

import com.safely.global.exception.BusinessException;
import com.safely.global.exception.ErrorCode;

public class LoginFailedException extends BusinessException {
    public LoginFailedException() {
        super(ErrorCode.LOGIN_FAILED);
    }
}