package com.ptit.projectmanagementbe.service;

import com.ptit.projectmanagementbe.dto.common.PageResponse;
import com.ptit.projectmanagementbe.dto.request.task.TaskCreateRequest;
import com.ptit.projectmanagementbe.dto.request.task.TaskUpdateRequest;
import com.ptit.projectmanagementbe.dto.response.task.TaskResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public interface TaskService {

    /**
     * Create a new task
     *
     * @param request the task create request
     * @param locale  the current locale
     * @return the created task response
     */
    TaskResponse createTask(TaskCreateRequest request, Locale locale);

    /**
     * Get a task by uuid
     *
     * @param uuid   the task uuid
     * @param locale the current locale
     * @return the task response
     */
    TaskResponse getTaskByUuid(String uuid, Locale locale);

    /**
     * Get all tasks for a project with pagination
     *
     * @param projectUuid the project uuid
     * @param pageable    the pageable information
     * @param locale      the current locale
     * @return the page of task responses
     */
    PageResponse<TaskResponse> getAllTasksByProject(String projectUuid, Pageable pageable, Locale locale);

    /**
     * Get all tasks for a project by status with pagination
     *
     * @param projectUuid the project uuid
     * @param status      the task status
     * @param pageable    the pageable information
     * @param locale      the current locale
     * @return the page of task responses
     */
    PageResponse<TaskResponse> getAllTasksByProjectAndStatus(String projectUuid, String status, Pageable pageable, Locale locale);

    /**
     * Update a task by uuid
     *
     * @param uuid    the task uuid
     * @param request the task update request
     * @param locale  the current locale
     * @return the updated task response
     */
    TaskResponse updateTask(String uuid, TaskUpdateRequest request, Locale locale);

    /**
     * Delete a task by uuid (soft delete)
     *
     * @param uuid   the task uuid
     * @param locale the current locale
     */
    void deleteTask(String uuid, Locale locale);

    /**
     * Get a list of active tasks with high priority for a user
     *
     * @param userUuid the user uuid
     * @param locale   the current locale
     * @return the list of task responses
     */
    List<TaskResponse> getHighPriorityTasksForUser(String userUuid, Locale locale);

    /**
     * Get tasks due between given dates for a user
     *
     * @param userUuid  the user uuid
     * @param startDate the start date
     * @param endDate   the end date
     * @param locale    the current locale
     * @return the list of task responses
     */
    List<TaskResponse> getTasksDueBetweenDates(String userUuid, LocalDate startDate, LocalDate endDate, Locale locale);
}