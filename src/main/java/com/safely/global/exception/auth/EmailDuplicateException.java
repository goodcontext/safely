package com.safely.global.exception.auth;

import com.safely.global.exception.BusinessException;
import com.safely.global.exception.ErrorCode;

public class EmailDuplicateException extends BusinessException {
    public EmailDuplicateException() {
        super(ErrorCode.EMAIL_DUPLICATION);
    }
}