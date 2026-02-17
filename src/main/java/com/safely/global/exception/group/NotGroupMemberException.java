package com.safely.global.exception.group;

import com.safely.global.exception.BusinessException;
import com.safely.global.exception.ErrorCode;

public class NotGroupMemberException extends BusinessException {
    public NotGroupMemberException() {
        super(ErrorCode.NOT_GROUP_MEMBER);
    }
}