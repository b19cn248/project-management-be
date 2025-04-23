package com.ptit.projectmanagementbe.exception;


import com.ptit.projectmanagementbe.constant.ErrorCode;

public class BadRequestException extends ApplicationException {

    public BadRequestException(String message) {
        super(message, ErrorCode.BAD_REQUEST);
    }

    public BadRequestException(String message, Object... args) {
        super(message, ErrorCode.BAD_REQUEST, args);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause, ErrorCode.BAD_REQUEST);
    }

    public BadRequestException(String message, Throwable cause, Object... args) {
        super(message, cause, ErrorCode.BAD_REQUEST, args);
    }
}
