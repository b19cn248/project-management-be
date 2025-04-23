package com.ptit.projectmanagementbe.service.impl;

import com.ptit.projectmanagementbe.constant.ErrorCode;
import com.ptit.projectmanagementbe.dto.common.PageResponse;
import com.ptit.projectmanagementbe.dto.request.project.ProjectCreateRequest;
import com.ptit.projectmanagementbe.dto.request.project.ProjectUpdateRequest;
import com.ptit.projectmanagementbe.dto.response.project.ProjectResponse;
import com.ptit.projectmanagementbe.entity.Project;
import com.ptit.projectmanagementbe.entity.User;
import com.ptit.projectmanagementbe.exception.BadRequestException;
import com.ptit.projectmanagementbe.exception.ResourceNotFoundException;
import com.ptit.projectmanagementbe.repository.ProjectRepository;
import com.ptit.projectmanagementbe.repository.TaskRepository;
import com.ptit.projectmanagementbe.repository.UserRepository;
import com.ptit.projectmanagementbe.service.ProjectService;
import com.ptit.projectmanagementbe.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final MessageUtil messageUtil;

    @Override
    @Transactional
    public ProjectResponse createProject(String userUuid, ProjectCreateRequest request, Locale locale) {
        log.debug("Creating project for user with uuid: {}", userUuid);

        // Find the user
        User user = findUserByUuid(userUuid, locale);

        // Check if project name already exists for this user
        if (projectRepository.existsByNameAndUserIdAndIsDeletedFalse(request.getName(), user.getId())) {
            throw new BadRequestException(
                    messageUtil.getMessage("project.name.exists", locale, request.getName()),
                    ErrorCode.PROJECT_ALREADY_EXISTS,
                    request.getName()
            );
        }

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException(
                    messageUtil.getMessage("project.invalid.date", locale),
                    ErrorCode.BAD_REQUEST
            );
        }

        // Create new project
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setStatus(request.getStatus());
        project.setUserId(user.getId());
        project.setCreatedBy(user.getName());
        project.setUpdatedBy(user.getName());

        Project savedProject = projectRepository.save(project);
        log.debug("Project created with id: {}", savedProject.getId());

        return mapToProjectResponse(savedProject, 0L, 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectByUuid(String uuid, Locale locale) {
        log.debug("Getting project by uuid: {}", uuid);

        Project project = findProjectByUuid(uuid, locale);

        // Get task counts for progress calculation
        Long totalTasks = taskRepository.countByProjectId(project.getId());
        Long completedTasks = taskRepository.countCompletedTasksByProjectId(project.getId());

        return mapToProjectResponse(project, completedTasks, totalTasks);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> getAllProjectsByUser(String userUuid, Pageable pageable, Locale locale) {
        log.debug("Getting all projects for user with uuid: {}", userUuid);

        // Find the user
        User user = findUserByUuid(userUuid, locale);

        Page<Project> projectPage = projectRepository.findByUserId(user.getId(), pageable);
        Page<ProjectResponse> projectResponsePage = projectPage.map(project -> {
            // Get task counts for progress calculation
            Long totalTasks = taskRepository.countByProjectId(project.getId());
            Long completedTasks = taskRepository.countCompletedTasksByProjectId(project.getId());
            return mapToProjectResponse(project, completedTasks, totalTasks);
        });

        return PageResponse.fromPage(projectResponsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> getAllProjectsByUserAndStatus(String userUuid, String status, Pageable pageable, Locale locale) {
        log.debug("Getting all projects for user with uuid: {} and status: {}", userUuid, status);

        // Find the user
        User user = findUserByUuid(userUuid, locale);

        Page<Project> projectPage = projectRepository.findByUserIdAndStatus(user.getId(), status, pageable);
        Page<ProjectResponse> projectResponsePage = projectPage.map(project -> {
            // Get task counts for progress calculation
            Long totalTasks = taskRepository.countByProjectId(project.getId());
            Long completedTasks = taskRepository.countCompletedTasksByProjectId(project.getId());
            return mapToProjectResponse(project, completedTasks, totalTasks);
        });

        return PageResponse.fromPage(projectResponsePage);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(String uuid, ProjectUpdateRequest request, Locale locale) {
        log.debug("Updating project with uuid: {}", uuid);

        Project project = findProjectByUuid(uuid, locale);

        // Check if project name is being changed and already exists for this user
        if (request.getName() != null && !request.getName().equals(project.getName())
                && projectRepository.existsByNameAndUserIdAndIsDeletedFalse(request.getName(), project.getUserId())) {
            throw new BadRequestException(
                    messageUtil.getMessage("project.name.exists", locale, request.getName()),
                    ErrorCode.PROJECT_ALREADY_EXISTS,
                    request.getName()
            );
        }

        // Validate dates if both are provided
        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException(
                    messageUtil.getMessage("project.invalid.date", locale),
                    ErrorCode.BAD_REQUEST
            );
        } else if (request.getStartDate() != null && request.getEndDate() == null
                && project.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException(
                    messageUtil.getMessage("project.invalid.date", locale),
                    ErrorCode.BAD_REQUEST
            );
        } else if (request.getStartDate() == null && request.getEndDate() != null
                && request.getEndDate().isBefore(project.getStartDate())) {
            throw new BadRequestException(
                    messageUtil.getMessage("project.invalid.date", locale),
                    ErrorCode.BAD_REQUEST
            );
        }

        // Update project fields if provided
        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getStartDate() != null) {
            project.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            project.setEndDate(request.getEndDate());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        project.setUpdatedBy("system");

        Project updatedProject = projectRepository.save(project);
        log.debug("Project updated with id: {}", updatedProject.getId());

        // Get task counts for progress calculation
        Long totalTasks = taskRepository.countByProjectId(updatedProject.getId());
        Long completedTasks = taskRepository.countCompletedTasksByProjectId(updatedProject.getId());

        return mapToProjectResponse(updatedProject, completedTasks, totalTasks);
    }

    @Override
    @Transactional
    public void deleteProject(String uuid, Locale locale) {
        log.debug("Deleting project with uuid: {}", uuid);

        Project project = findProjectByUuid(uuid, locale);
        project.setIsDeleted(true);
        project.setUpdatedBy("system");

        projectRepository.save(project);
        log.debug("Project deleted with id: {}", project.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getActiveProjectsWithProgress(String userUuid, Locale locale) {
        log.debug("Getting active projects with progress for user with uuid: {}", userUuid);

        // Find the user
        User user = findUserByUuid(userUuid, locale);

        List<Project> projects = projectRepository.findActiveProjectsByUserIdOrderByEndDate(user.getId());

        return projects.stream().map(project -> {
            // Get task counts for progress calculation
            Long totalTasks = taskRepository.countByProjectId(project.getId());
            Long completedTasks = taskRepository.countCompletedTasksByProjectId(project.getId());
            return mapToProjectResponse(project, completedTasks, totalTasks);
        }).collect(Collectors.toList());
    }

    /**
     * Find a user by uuid
     *
     * @param uuid   the user uuid
     * @param locale the current locale
     * @return the user entity
     * @throws ResourceNotFoundException if the user is not found
     */
    private User findUserByUuid(String uuid, Locale locale) {
        return userRepository.findActiveByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("user.not.found", locale, "uuid", uuid),
                        "User", "uuid", uuid
                ));
    }

    /**
     * Find a project by uuid
     *
     * @param uuid   the project uuid
     * @param locale the current locale
     * @return the project entity
     * @throws ResourceNotFoundException if the project is not found
     */
    private Project findProjectByUuid(String uuid, Locale locale) {
        return projectRepository.findActiveByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("project.not.found", locale, "uuid", uuid),
                        "Project", "uuid", uuid
                ));
    }

    /**
     * Map a Project entity to a ProjectResponse DTO
     *
     * @param project        the project entity
     * @param completedTasks the number of completed tasks
     * @param totalTasks     the total number of tasks
     * @return the project response DTO
     */
    private ProjectResponse mapToProjectResponse(Project project, Long completedTasks, Long totalTasks) {
        Double progressPercentage = totalTasks > 0
                ? (double) completedTasks / totalTasks * 100
                : 0.0;

        return ProjectResponse.builder()
                .uuid(project.getUuid())
                .name(project.getName())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .status(project.getStatus())
                .completedTasks(completedTasks.intValue())
                .totalTasks(totalTasks.intValue())
                .progressPercentage(Math.round(progressPercentage * 100) / 100.0) // Round to 2 decimal places
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
