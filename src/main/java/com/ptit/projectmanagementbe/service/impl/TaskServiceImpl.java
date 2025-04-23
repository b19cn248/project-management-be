package com.ptit.projectmanagementbe.service.impl;

import com.ptit.projectmanagementbe.constant.ErrorCode;
import com.ptit.projectmanagementbe.dto.common.PageResponse;
import com.ptit.projectmanagementbe.dto.request.task.TaskCreateRequest;
import com.ptit.projectmanagementbe.dto.request.task.TaskUpdateRequest;
import com.ptit.projectmanagementbe.dto.response.task.TaskResponse;
import com.ptit.projectmanagementbe.entity.Project;
import com.ptit.projectmanagementbe.entity.Task;
import com.ptit.projectmanagementbe.entity.User;
import com.ptit.projectmanagementbe.exception.BadRequestException;
import com.ptit.projectmanagementbe.exception.ResourceNotFoundException;
import com.ptit.projectmanagementbe.repository.ProjectRepository;
import com.ptit.projectmanagementbe.repository.TaskRepository;
import com.ptit.projectmanagementbe.repository.UserRepository;
import com.ptit.projectmanagementbe.service.TaskService;
import com.ptit.projectmanagementbe.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final MessageUtil messageUtil;

    @Override
    @Transactional
    public TaskResponse createTask(TaskCreateRequest request, Locale locale) {
        log.debug("Creating task for project with uuid: {}", request.getProjectUuid());

        // Find the project
        Project project = findProjectByUuid(request.getProjectUuid(), locale);

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException(
                    messageUtil.getMessage("task.invalid.date", locale),
                    ErrorCode.BAD_REQUEST
            );
        }

        // Create new task
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStartDate(request.getStartDate());
        task.setEndDate(request.getEndDate());
        task.setPriority(request.getPriority());
        task.setStatus(request.getStatus());
        task.setProjectId(project.getId());
        task.setCreatedBy("system");
        task.setUpdatedBy("system");

        Task savedTask = taskRepository.save(task);
        log.debug("Task created with id: {}", savedTask.getId());

        return mapToTaskResponse(savedTask, project);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskByUuid(String uuid, Locale locale) {
        log.debug("Getting task by uuid: {}", uuid);

        Task task = findTaskByUuid(uuid, locale);
        Project project = findProjectById(task.getProjectId(), locale);

        return mapToTaskResponse(task, project);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> getAllTasksByProject(String projectUuid, Pageable pageable, Locale locale) {
        log.debug("Getting all tasks for project with uuid: {}", projectUuid);

        // Find the project
        Project project = findProjectByUuid(projectUuid, locale);

        Page<Task> taskPage = taskRepository.findByProjectId(project.getId(), pageable);
        Page<TaskResponse> taskResponsePage = taskPage.map(task -> mapToTaskResponse(task, project));

        return PageResponse.fromPage(taskResponsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> getAllTasksByProjectAndStatus(String projectUuid, String status, Pageable pageable, Locale locale) {
        log.debug("Getting all tasks for project with uuid: {} and status: {}", projectUuid, status);

        // Find the project
        Project project = findProjectByUuid(projectUuid, locale);

        Page<Task> taskPage = taskRepository.findByProjectIdAndStatus(project.getId(), status, pageable);
        Page<TaskResponse> taskResponsePage = taskPage.map(task -> mapToTaskResponse(task, project));

        return PageResponse.fromPage(taskResponsePage);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(String uuid, TaskUpdateRequest request, Locale locale) {
        log.debug("Updating task with uuid: {}", uuid);

        Task task = findTaskByUuid(uuid, locale);
        Project project = findProjectById(task.getProjectId(), locale);

        // Validate dates if both are provided
        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException(
                    messageUtil.getMessage("task.invalid.date", locale),
                    ErrorCode.BAD_REQUEST
            );
        } else if (request.getStartDate() != null && request.getEndDate() == null
                && task.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException(
                    messageUtil.getMessage("task.invalid.date", locale),
                    ErrorCode.BAD_REQUEST
            );
        } else if (request.getStartDate() == null && request.getEndDate() != null
                && request.getEndDate().isBefore(task.getStartDate())) {
            throw new BadRequestException(
                    messageUtil.getMessage("task.invalid.date", locale),
                    ErrorCode.BAD_REQUEST
            );
        }

        // Update task fields if provided
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStartDate() != null) {
            task.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            task.setEndDate(request.getEndDate());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        task.setUpdatedBy("system");

        Task updatedTask = taskRepository.save(task);
        log.debug("Task updated with id: {}", updatedTask.getId());

        return mapToTaskResponse(updatedTask, project);
    }

    @Override
    @Transactional
    public void deleteTask(String uuid, Locale locale) {
        log.debug("Deleting task with uuid: {}", uuid);

        Task task = findTaskByUuid(uuid, locale);
        task.setIsDeleted(true);
        task.setUpdatedBy("system");

        taskRepository.save(task);
        log.debug("Task deleted with id: {}", task.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getHighPriorityTasksForUser(String userUuid, Locale locale) {
        log.debug("Getting high priority tasks for user with uuid: {}", userUuid);

        // Find the user
        User user = findUserByUuid(userUuid, locale);

        List<Task> tasks = taskRepository.findActiveTasksByUserIdOrderByPriorityAndEndDate(user.getId());

        return tasks.stream().map(task -> {
            Project project = findProjectById(task.getProjectId(), locale);
            return mapToTaskResponse(task, project);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksDueBetweenDates(String userUuid, LocalDate startDate, LocalDate endDate, Locale locale) {
        log.debug("Getting tasks due between {} and {} for user with uuid: {}", startDate, endDate, userUuid);

        // Find the user
        User user = findUserByUuid(userUuid, locale);

        List<Task> tasks = taskRepository.findTasksDueBetweenDates(user.getId(), startDate, endDate);

        return tasks.stream().map(task -> {
            Project project = findProjectById(task.getProjectId(), locale);
            return mapToTaskResponse(task, project);
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
     * Find a project by id
     *
     * @param id     the project id
     * @param locale the current locale
     * @return the project entity
     * @throws ResourceNotFoundException if the project is not found
     */
    private Project findProjectById(Integer id, Locale locale) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("project.not.found", locale, "id", id),
                        "Project", "id", id
                ));
    }

    /**
     * Find a task by uuid
     *
     * @param uuid   the task uuid
     * @param locale the current locale
     * @return the task entity
     * @throws ResourceNotFoundException if the task is not found
     */
    private Task findTaskByUuid(String uuid, Locale locale) {
        return taskRepository.findActiveByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("task.not.found", locale, "uuid", uuid),
                        "Task", "uuid", uuid
                ));
    }

    /**
     * Map a Task entity to a TaskResponse DTO
     *
     * @param task    the task entity
     * @param project the associated project entity
     * @return the task response DTO
     */
    private TaskResponse mapToTaskResponse(Task task, Project project) {
        return TaskResponse.builder()
                .uuid(task.getUuid())
                .title(task.getTitle())
                .description(task.getDescription())
                .startDate(task.getStartDate())
                .endDate(task.getEndDate())
                .priority(task.getPriority())
                .status(task.getStatus())
                .projectUuid(project.getUuid())
                .projectName(project.getName())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
