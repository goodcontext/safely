package com.safely.global.exception.upload;

import com.safely.global.exception.BusinessException;
import com.safely.global.exception.ErrorCode;

public class InvalidFileExtensionException extends BusinessException {
    public InvalidFileExtensionException() {
        super(ErrorCode.INVALID_FILE_EXTENSION);
    }
}