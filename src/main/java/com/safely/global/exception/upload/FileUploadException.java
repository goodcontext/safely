package com.safely.global.exception.upload;

import com.safely.global.exception.BusinessException;
import com.safely.global.exception.ErrorCode;

public class FileUploadException extends BusinessException {
    public FileUploadException() {
        super(ErrorCode.FILE_UPLOAD_FAILED);
    }
}