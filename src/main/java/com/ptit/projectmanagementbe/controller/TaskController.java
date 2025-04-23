package com.ptit.projectmanagementbe.controller;

import com.ptit.projectmanagementbe.constant.ApiConstant;
import com.ptit.projectmanagementbe.constant.SuccessCode;
import com.ptit.projectmanagementbe.dto.common.ApiResponse;
import com.ptit.projectmanagementbe.dto.common.PageResponse;
import com.ptit.projectmanagementbe.dto.request.task.TaskCreateRequest;
import com.ptit.projectmanagementbe.dto.request.task.TaskUpdateRequest;
import com.ptit.projectmanagementbe.dto.response.task.TaskResponse;
import com.ptit.projectmanagementbe.service.TaskService;
import com.ptit.projectmanagementbe.util.MessageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
@Tag(name = "Task", description = "Task API")
public class TaskController {

    private final TaskService taskService;
    private final MessageUtil messageUtil;

    @PostMapping(ApiConstant.TASKS)
    @Operation(summary = "Create a new task")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody TaskCreateRequest request,
            Locale locale
    ) {
        TaskResponse response = taskService.createTask(request, locale);
        String message = messageUtil.getMessage("task.created", locale);
        return new ResponseEntity<>(
                ApiResponse.success(SuccessCode.TASK_CREATED, message, response),
                HttpStatus.CREATED
        );
    }

    @GetMapping(ApiConstant.TASK_BY_UUID)
    @Operation(summary = "Get a task by UUID")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskByUuid(
            @Parameter(description = "Task UUID", required = true)
            @PathVariable(ApiConstant.UUID) String uuid,
            Locale locale
    ) {
        TaskResponse response = taskService.getTaskByUuid(uuid, locale);
        String message = messageUtil.getMessage("task.found", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.TASK_FOUND, message, response)
        );
    }

    @GetMapping(ApiConstant.PROJECT_TASKS)
    @Operation(summary = "Get all tasks for a project with pagination")
    public ResponseEntity<ApiResponse<PageResponse<TaskResponse>>> getAllTasksByProject(
            @Parameter(description = "Project UUID", required = true)
            @PathVariable(ApiConstant.UUID) String uuid,
            @Parameter(description = "Task status (optional)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(value = ApiConstant.PAGE, defaultValue = ApiConstant.DEFAULT_PAGE) int page,
            @Parameter(description = "Page size")
            @RequestParam(value = ApiConstant.SIZE, defaultValue = ApiConstant.DEFAULT_SIZE) int size,
            @Parameter(description = "Sort by field (format: field,direction - e.g. id,desc)")
            @RequestParam(value = ApiConstant.SORT, defaultValue = ApiConstant.DEFAULT_SORT) String sort,
            Locale locale
    ) {
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, direction, sortField);
        PageResponse<TaskResponse> response;

        if (status != null && !status.isEmpty()) {
            response = taskService.getAllTasksByProjectAndStatus(uuid, status, pageable, locale);
        } else {
            response = taskService.getAllTasksByProject(uuid, pageable, locale);
        }

        String message = messageUtil.getMessage("tasks.found", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.TASKS_FOUND, message, response)
        );
    }

    @GetMapping(ApiConstant.TASKS + "/priority")
    @Operation(summary = "Get high priority tasks for a user")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getHighPriorityTasksForUser(
            @Parameter(description = "User UUID", required = true)
            @RequestParam String userUuid,
            Locale locale
    ) {
        List<TaskResponse> response = taskService.getHighPriorityTasksForUser(userUuid, locale);
        String message = messageUtil.getMessage("tasks.found", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.TASKS_FOUND, message, response)
        );
    }

    @GetMapping(ApiConstant.TASKS + "/due")
    @Operation(summary = "Get tasks due between dates for a user")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksDueBetweenDates(
            @Parameter(description = "User UUID", required = true)
            @RequestParam String userUuid,
            @Parameter(description = "Start date (format: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (format: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Locale locale
    ) {
        List<TaskResponse> response = taskService.getTasksDueBetweenDates(userUuid, startDate, endDate, locale);
        String message = messageUtil.getMessage("tasks.found", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.TASKS_FOUND, message, response)
        );
    }

    @PutMapping(ApiConstant.TASK_BY_UUID)
    @Operation(summary = "Update a task by UUID")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @Parameter(description = "Task UUID", required = true)
            @PathVariable(ApiConstant.UUID) String uuid,
            @Valid @RequestBody TaskUpdateRequest request,
            Locale locale
    ) {
        TaskResponse response = taskService.updateTask(uuid, request, locale);
        String message = messageUtil.getMessage("task.updated", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.TASK_UPDATED, message, response)
        );
    }

    @DeleteMapping(ApiConstant.TASK_BY_UUID)
    @Operation(summary = "Delete a task by UUID")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @Parameter(description = "Task UUID", required = true)
            @PathVariable(ApiConstant.UUID) String uuid,
            Locale locale
    ) {
        taskService.deleteTask(uuid, locale);
        String message = messageUtil.getMessage("task.deleted", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.TASK_DELETED, message)
        );
    }
}
