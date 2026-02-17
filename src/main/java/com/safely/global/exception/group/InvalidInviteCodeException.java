package com.safely.global.exception.group;

import com.safely.global.exception.BusinessException;
import com.safely.global.exception.ErrorCode;

public class InvalidInviteCodeException extends BusinessException {
    public InvalidInviteCodeException() {
        super(ErrorCode.INVALID_INVITE_CODE);
    }
}