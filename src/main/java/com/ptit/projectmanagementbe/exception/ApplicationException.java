package com.ptit.projectmanagementbe.exception;

import lombok.Getter;

@Getter
public class ApplicationException extends RuntimeException {

    private final String code;
    private final Object[] args;

    public ApplicationException(String message, String code) {
        super(message);
        this.code = code;
        this.args = new Object[]{};
    }

    public ApplicationException(String message, String code, Object... args) {
        super(message);
        this.code = code;
        this.args = args;
    }

    public ApplicationException(String message, Throwable cause, String code) {
        super(message, cause);
        this.code = code;
        this.args = new Object[]{};
    }

    public ApplicationException(String message, Throwable cause, String code, Object... args) {
        super(message, cause);
        this.code = code;
        this.args = args;
    }
}
