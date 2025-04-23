package com.ptit.projectmanagementbe.exception;


import com.ptit.projectmanagementbe.constant.ErrorCode;

public class ForbiddenException extends ApplicationException {

    public ForbiddenException(String message) {
        super(message, ErrorCode.FORBIDDEN);
    }

    public ForbiddenException(String message, Object... args) {
        super(message, ErrorCode.FORBIDDEN, args);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause, ErrorCode.FORBIDDEN);
    }

    public ForbiddenException(String message, Throwable cause, Object... args) {
        super(message, cause, ErrorCode.FORBIDDEN, args);
    }
}
