package com.safely.global.exception.group;

import com.safely.global.exception.BusinessException;
import com.safely.global.exception.ErrorCode;

public class AlreadyJoinedGroupException extends BusinessException {
    public AlreadyJoinedGroupException() {
        super(ErrorCode.ALREADY_JOINED_GROUP);
    }
}