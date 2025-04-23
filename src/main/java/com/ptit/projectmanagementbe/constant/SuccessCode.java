package com.ptit.projectmanagementbe.constant;


public class SuccessCode {

    // Common success codes
    public static final String OK = "S0001";
    public static final String CREATED = "S0002";
    public static final String UPDATED = "S0003";
    public static final String DELETED = "S0004";

    // User related success codes
    public static final String USER_CREATED = "S1001";
    public static final String USER_UPDATED = "S1002";
    public static final String USER_DELETED = "S1003";
    public static final String USER_FOUND = "S1004";
    public static final String USERS_FOUND = "S1005";

    // Project related success codes
    public static final String PROJECT_CREATED = "S2001";
    public static final String PROJECT_UPDATED = "S2002";
    public static final String PROJECT_DELETED = "S2003";
    public static final String PROJECT_FOUND = "S2004";
    public static final String PROJECTS_FOUND = "S2005";

    // Task related success codes
    public static final String TASK_CREATED = "S3001";
    public static final String TASK_UPDATED = "S3002";
    public static final String TASK_DELETED = "S3003";
    public static final String TASK_FOUND = "S3004";
    public static final String TASKS_FOUND = "S3005";

    // Meeting related success codes
    public static final String MEETING_CREATED = "S4001";
    public static final String MEETING_UPDATED = "S4002";
    public static final String MEETING_DELETED = "S4003";
    public static final String MEETING_FOUND = "S4004";
    public static final String MEETINGS_FOUND = "S4005";

    // Schedule related success codes
    public static final String SCHEDULE_CREATED = "S5001";
    public static final String SCHEDULE_UPDATED = "S5002";
    public static final String SCHEDULE_DELETED = "S5003";
    public static final String SCHEDULE_FOUND = "S5004";
    public static final String SCHEDULES_FOUND = "S5005";

    // File related success codes
    public static final String FILE_UPLOADED = "S6001";
    public static final String FILE_DOWNLOADED = "S6002";
    public static final String FILE_DELETED = "S6003";
    public static final String FILE_FOUND = "S6004";
    public static final String FILES_FOUND = "S6005";

    private SuccessCode() {
        // Private constructor to prevent instantiation
    }
}
