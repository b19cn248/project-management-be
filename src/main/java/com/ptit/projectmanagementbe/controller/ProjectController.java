package com.ptit.projectmanagementbe.controller;

import com.ptit.projectmanagementbe.constant.ApiConstant;
import com.ptit.projectmanagementbe.constant.SuccessCode;
import com.ptit.projectmanagementbe.dto.common.ApiResponse;
import com.ptit.projectmanagementbe.dto.common.PageResponse;
import com.ptit.projectmanagementbe.dto.request.project.ProjectCreateRequest;
import com.ptit.projectmanagementbe.dto.request.project.ProjectUpdateRequest;
import com.ptit.projectmanagementbe.dto.response.project.ProjectResponse;
import com.ptit.projectmanagementbe.service.ProjectService;
import com.ptit.projectmanagementbe.util.MessageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
@Tag(name = "Project", description = "Project API")
public class ProjectController {

    private final ProjectService projectService;
    private final MessageUtil messageUtil;

    @PostMapping(ApiConstant.PROJECTS)
    @Operation(summary = "Create a new project for a user")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Parameter(description = "User UUID", required = true)
            @RequestParam String userUuid,
            @Valid @RequestBody ProjectCreateRequest request,
            Locale locale
    ) {
        ProjectResponse response = projectService.createProject(userUuid, request, locale);
        String message = messageUtil.getMessage("project.created", locale);
        return new ResponseEntity<>(
                ApiResponse.success(SuccessCode.PROJECT_CREATED, message, response),
                HttpStatus.CREATED
        );
    }

    @GetMapping(ApiConstant.PROJECT_BY_UUID)
    @Operation(summary = "Get a project by UUID")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectByUuid(
            @Parameter(description = "Project UUID", required = true)
            @PathVariable(ApiConstant.UUID) String uuid,
            Locale locale
    ) {
        ProjectResponse response = projectService.getProjectByUuid(uuid, locale);
        String message = messageUtil.getMessage("project.found", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.PROJECT_FOUND, message, response)
        );
    }

    @GetMapping(ApiConstant.PROJECTS)
    @Operation(summary = "Get all projects for a user with pagination")
    public ResponseEntity<ApiResponse<PageResponse<ProjectResponse>>> getAllProjectsByUser(
            @Parameter(description = "User UUID", required = true)
            @RequestParam String userUuid,
            @Parameter(description = "Project status (optional)")
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
        PageResponse<ProjectResponse> response;

        if (status != null && !status.isEmpty()) {
            response = projectService.getAllProjectsByUserAndStatus(userUuid, status, pageable, locale);
        } else {
            response = projectService.getAllProjectsByUser(userUuid, pageable, locale);
        }

        String message = messageUtil.getMessage("projects.found", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.PROJECTS_FOUND, message, response)
        );
    }

    @GetMapping(ApiConstant.PROJECTS + "/active")
    @Operation(summary = "Get active projects with progress information for a user")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getActiveProjectsWithProgress(
            @Parameter(description = "User UUID", required = true)
            @RequestParam String userUuid,
            Locale locale
    ) {
        List<ProjectResponse> response = projectService.getActiveProjectsWithProgress(userUuid, locale);
        String message = messageUtil.getMessage("projects.found", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.PROJECTS_FOUND, message, response)
        );
    }

    @PutMapping(ApiConstant.PROJECT_BY_UUID)
    @Operation(summary = "Update a project by UUID")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @Parameter(description = "Project UUID", required = true)
            @PathVariable(ApiConstant.UUID) String uuid,
            @Valid @RequestBody ProjectUpdateRequest request,
            Locale locale
    ) {
        ProjectResponse response = projectService.updateProject(uuid, request, locale);
        String message = messageUtil.getMessage("project.updated", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.PROJECT_UPDATED, message, response)
        );
    }

    @DeleteMapping(ApiConstant.PROJECT_BY_UUID)
    @Operation(summary = "Delete a project by UUID")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @Parameter(description = "Project UUID", required = true)
            @PathVariable(ApiConstant.UUID) String uuid,
            Locale locale
    ) {
        projectService.deleteProject(uuid, locale);
        String message = messageUtil.getMessage("project.deleted", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.PROJECT_DELETED, message)
        );
    }
}
