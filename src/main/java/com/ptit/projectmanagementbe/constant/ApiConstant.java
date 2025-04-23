package com.ptit.projectmanagementbe.constant;

public class ApiConstant {

    // Base API paths
    public static final String API_V1 = "/api/v1";

    // User API paths
    public static final String USERS = API_V1 + "/users";
    public static final String USER_BY_UUID = USERS + "/{uuid}";

    // Project API paths
    public static final String PROJECTS = API_V1 + "/projects";
    public static final String PROJECT_BY_UUID = PROJECTS + "/{uuid}";

    // Task API paths
    public static final String TASKS = API_V1 + "/tasks";
    public static final String TASK_BY_UUID = TASKS + "/{uuid}";
    public static final String PROJECT_TASKS = PROJECT_BY_UUID + "/tasks";

    // Meeting API paths
    public static final String MEETINGS = API_V1 + "/meetings";
    public static final String MEETING_BY_UUID = MEETINGS + "/{uuid}";

    // Schedule API paths
    public static final String SCHEDULES = API_V1 + "/schedules";
    public static final String SCHEDULE_BY_UUID = SCHEDULES + "/{uuid}";

    // File API paths
    public static final String FILES = API_V1 + "/files";
    public static final String FILE_BY_UUID = FILES + "/{uuid}";
    public static final String UPLOAD_FILE = FILES + "/upload";

    // Common path variables
    public static final String UUID = "uuid";

    // Common request parameters
    public static final String PAGE = "page";
    public static final String SIZE = "size";
    public static final String SORT = "sort";

    // Default pagination values
    public static final String DEFAULT_PAGE = "0";
    public static final String DEFAULT_SIZE = "10";
    public static final String DEFAULT_SORT = "id,desc";

    private ApiConstant() {
        // Private constructor to prevent instantiation
    }
}
