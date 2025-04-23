package com.ptit.projectmanagementbe.constant;

public class ErrorCode {

    // Common error codes
    public static final String INTERNAL_SERVER_ERROR = "E0001";
    public static final String BAD_REQUEST = "E0002";
    public static final String RESOURCE_NOT_FOUND = "E0003";
    public static final String VALIDATION_ERROR = "E0004";
    public static final String UNAUTHORIZED = "E0005";
    public static final String FORBIDDEN = "E0006";

    // User related error codes
    public static final String USER_NOT_FOUND = "E1001";
    public static final String USER_ALREADY_EXISTS = "E1002";
    public static final String INVALID_CREDENTIALS = "E1003";

    // Project related error codes
    public static final String PROJECT_NOT_FOUND = "E2001";
    public static final String PROJECT_ALREADY_EXISTS = "E2002";

    // Task related error codes
    public static final String TASK_NOT_FOUND = "E3001";
    public static final String TASK_ALREADY_EXISTS = "E3002";

    // Meeting related error codes
    public static final String MEETING_NOT_FOUND = "E4001";
    public static final String MEETING_ALREADY_EXISTS = "E4002";
    public static final String MEETING_TIME_CONFLICT = "E4003";

    // Schedule related error codes
    public static final String SCHEDULE_NOT_FOUND = "E5001";
    public static final String SCHEDULE_ALREADY_EXISTS = "E5002";
    public static final String SCHEDULE_TIME_CONFLICT = "E5003";

    // File related error codes
    public static final String FILE_NOT_FOUND = "E6001";
    public static final String FILE_UPLOAD_ERROR = "E6002";
    public static final String FILE_DOWNLOAD_ERROR = "E6003";
    public static final String INVALID_FILE_TYPE = "E6004";
    public static final String FILE_SIZE_EXCEEDED = "E6005";

    private ErrorCode() {
        // Private constructor to prevent instantiation
    }
}
