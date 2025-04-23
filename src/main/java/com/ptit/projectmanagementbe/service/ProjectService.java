package com.ptit.projectmanagementbe.service;

import com.ptit.projectmanagementbe.dto.common.PageResponse;
import com.ptit.projectmanagementbe.dto.request.project.ProjectCreateRequest;
import com.ptit.projectmanagementbe.dto.request.project.ProjectUpdateRequest;
import com.ptit.projectmanagementbe.dto.response.project.ProjectResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Locale;

public interface ProjectService {

    /**
     * Create a new project for a user
     *
     * @param userUuid the user uuid
     * @param request  the project create request
     * @param locale   the current locale
     * @return the created project response
     */
    ProjectResponse createProject(String userUuid, ProjectCreateRequest request, Locale locale);

    /**
     * Get a project by uuid
     *
     * @param uuid   the project uuid
     * @param locale the current locale
     * @return the project response
     */
    ProjectResponse getProjectByUuid(String uuid, Locale locale);

    /**
     * Get all projects for a user with pagination
     *
     * @param userUuid the user uuid
     * @param pageable the pageable information
     * @param locale   the current locale
     * @return the page of project responses
     */
    PageResponse<ProjectResponse> getAllProjectsByUser(String userUuid, Pageable pageable, Locale locale);

    /**
     * Get all projects for a user by status with pagination
     *
     * @param userUuid the user uuid
     * @param status   the project status
     * @param pageable the pageable information
     * @param locale   the current locale
     * @return the page of project responses
     */
    PageResponse<ProjectResponse> getAllProjectsByUserAndStatus(String userUuid, String status, Pageable pageable, Locale locale);

    /**
     * Update a project by uuid
     *
     * @param uuid    the project uuid
     * @param request the project update request
     * @param locale  the current locale
     * @return the updated project response
     */
    ProjectResponse updateProject(String uuid, ProjectUpdateRequest request, Locale locale);

    /**
     * Delete a project by uuid (soft delete)
     *
     * @param uuid   the project uuid
     * @param locale the current locale
     */
    void deleteProject(String uuid, Locale locale);

    /**
     * Get a list of active projects with progress information for a user
     *
     * @param userUuid the user uuid
     * @param locale   the current locale
     * @return the list of project responses
     */
    List<ProjectResponse> getActiveProjectsWithProgress(String userUuid, Locale locale);
}
