package com.safely.global.exception.group;

import com.safely.global.exception.BusinessException;
import com.safely.global.exception.ErrorCode;

public class GroupPermissionDeniedException extends BusinessException {
    public GroupPermissionDeniedException() {
        super(ErrorCode.GROUP_PERMISSION_DENIED);
    }
}