package com.ptit.projectmanagementbe.exception;


import com.ptit.projectmanagementbe.constant.ErrorCode;

public class ResourceNotFoundException extends ApplicationException {

    public ResourceNotFoundException(String message) {
        super(message, ErrorCode.RESOURCE_NOT_FOUND);
    }

    public ResourceNotFoundException(String message, Object... args) {
        super(message, ErrorCode.RESOURCE_NOT_FOUND, args);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
                String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue),
                ErrorCode.RESOURCE_NOT_FOUND,
                resourceName, fieldName, fieldValue
        );
    }
}
