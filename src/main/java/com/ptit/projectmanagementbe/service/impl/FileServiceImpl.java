package com.ptit.projectmanagementbe.service.impl;

import com.ptit.projectmanagementbe.constant.ErrorCode;
import com.ptit.projectmanagementbe.dto.common.PageResponse;
import com.ptit.projectmanagementbe.dto.request.file.FileUploadRequest;
import com.ptit.projectmanagementbe.dto.response.file.FileResponse;
import com.ptit.projectmanagementbe.entity.File;
import com.ptit.projectmanagementbe.entity.Project;
import com.ptit.projectmanagementbe.entity.Task;
import com.ptit.projectmanagementbe.entity.User;
import com.ptit.projectmanagementbe.exception.BadRequestException;
import com.ptit.projectmanagementbe.exception.ResourceNotFoundException;
import com.ptit.projectmanagementbe.repository.FileRepository;
import com.ptit.projectmanagementbe.repository.ProjectRepository;
import com.ptit.projectmanagementbe.repository.TaskRepository;
import com.ptit.projectmanagementbe.repository.UserRepository;
import com.ptit.projectmanagementbe.service.FileService;
import com.ptit.projectmanagementbe.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final MessageUtil messageUtil;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    // List of allowed file extensions
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv",
            "jpg", "jpeg", "png", "gif", "zip", "rar"
    );

    // Maximum file size in bytes (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Override
    @Transactional
    public FileResponse uploadFile(MultipartFile file, Locale locale) {
        log.debug("Uploading file: {}", file.getOriginalFilename());

        // Validate file
        validateFile(file, locale);

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = getFileExtension(originalFilename);
            String filename = UUID.randomUUID().toString() + "." + extension;

            // Copy file to upload directory
            Path targetLocation = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Create file entity
            File fileEntity = new File();
            fileEntity.setFileName(originalFilename);
            fileEntity.setFilePath(filename);
            fileEntity.setFileSize(file.getSize());
            fileEntity.setContentType(file.getContentType());
            fileEntity.setCreatedBy("system");
            fileEntity.setUpdatedBy("system");

            File savedFile = fileRepository.save(fileEntity);
            log.debug("File uploaded with id: {}", savedFile.getId());

            return mapToFileResponse(savedFile);
        } catch (IOException ex) {
            throw new BadRequestException(
                    messageUtil.getMessage("file.upload.error", locale),
                    ex,
                    ErrorCode.FILE_UPLOAD_ERROR
            );
        }
    }

    @Override
    @Transactional
    public FileResponse associateFile(String fileUuid, FileUploadRequest request, Locale locale) {
        log.debug("Associating file with uuid: {} to task/project", fileUuid);

        File file = findFileByUuid(fileUuid, locale);

        // Associate with task or project if provided
        Integer taskId = null;
        Integer projectId = null;

        if (request.getTaskUuid() != null && !request.getTaskUuid().isEmpty()) {
            Task task = findTaskByUuid(request.getTaskUuid(), locale);
            taskId = task.getId();
        }

        if (request.getProjectUuid() != null && !request.getProjectUuid().isEmpty()) {
            Project project = findProjectByUuid(request.getProjectUuid(), locale);
            projectId = project.getId();
        }

        // Update file entity
        file.setTaskId(taskId);
        file.setProjectId(projectId);
        file.setUpdatedBy("system");

        File updatedFile = fileRepository.save(file);
        log.debug("File associated with id: {}", updatedFile.getId());

        return mapToFileResponse(updatedFile);
    }

    @Override
    @Transactional(readOnly = true)
    public FileResponse getFileByUuid(String uuid, Locale locale) {
        log.debug("Getting file by uuid: {}", uuid);

        File file = findFileByUuid(uuid, locale);
        return mapToFileResponse(file);
    }

    @Override
    public Resource downloadFile(String uuid, Locale locale) {
        log.debug("Downloading file with uuid: {}", uuid);

        try {
            File file = findFileByUuid(uuid, locale);
            Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(file.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new ResourceNotFoundException(
                        messageUtil.getMessage("file.not.found", locale, "uuid", uuid),
                        "File", "uuid", uuid
                );
            }
        } catch (MalformedURLException ex) {
            throw new BadRequestException(
                    messageUtil.getMessage("file.download.error", locale),
                    ex,
                    ErrorCode.FILE_DOWNLOAD_ERROR
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FileResponse> getAllFilesByTask(String taskUuid, Pageable pageable, Locale locale) {
        log.debug("Getting all files for task with uuid: {}", taskUuid);

        // Find the task
        Task task = findTaskByUuid(taskUuid, locale);

        Page<File> filePage = fileRepository.findActiveByTaskId(task.getId(), pageable);
        Page<FileResponse> fileResponsePage = filePage.map(this::mapToFileResponse);

        return PageResponse.fromPage(fileResponsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FileResponse> getAllFilesByProject(String projectUuid, Pageable pageable, Locale locale) {
        log.debug("Getting all files for project with uuid: {}", projectUuid);

        // Find the project
        Project project = findProjectByUuid(projectUuid, locale);

        Page<File> filePage = fileRepository.findActiveByProjectId(project.getId(), pageable);
        Page<FileResponse> fileResponsePage = filePage.map(this::mapToFileResponse);

        return PageResponse.fromPage(fileResponsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FileResponse> getAllFilesByUser(String userUuid, Pageable pageable, Locale locale) {
        log.debug("Getting all files for user with uuid: {}", userUuid);

        // Find the user
        User user = findUserByUuid(userUuid, locale);

        Page<File> filePage = fileRepository.findByUserId(user.getId(), pageable);
        Page<FileResponse> fileResponsePage = filePage.map(this::mapToFileResponse);

        return PageResponse.fromPage(fileResponsePage);
    }

    @Override
    @Transactional
    public void deleteFile(String uuid, Locale locale) {
        log.debug("Deleting file with uuid: {}", uuid);

        File file = findFileByUuid(uuid, locale);
        file.setIsDeleted(true);
        file.setUpdatedBy("system");

        fileRepository.save(file);
        log.debug("File deleted with id: {}", file.getId());
    }

    /**
     * Validate file before upload
     *
     * @param file   the multipart file
     * @param locale the current locale
     * @throws BadRequestException if file is invalid
     */
    private void validateFile(MultipartFile file, Locale locale) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new BadRequestException(
                    messageUtil.getMessage("file.empty", locale),
                    ErrorCode.BAD_REQUEST
            );
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException(
                    messageUtil.getMessage("file.size.exceeded", locale),
                    ErrorCode.FILE_SIZE_EXCEEDED
            );
        }

        // Check file extension
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(originalFilename);

        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BadRequestException(
                    messageUtil.getMessage("file.type.not.supported", locale),
                    ErrorCode.INVALID_FILE_TYPE
            );
        }
    }

    /**
     * Extract file extension from filename
     *
     * @param filename the filename
     * @return the extension
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
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
     * Find a file by uuid
     *
     * @param uuid   the file uuid
     * @param locale the current locale
     * @return the file entity
     * @throws ResourceNotFoundException if the file is not found
     */
    private File findFileByUuid(String uuid, Locale locale) {
        return fileRepository.findActiveByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("file.not.found", locale, "uuid", uuid),
                        "File", "uuid", uuid
                ));
    }

    /**
     * Map a File entity to a FileResponse DTO
     *
     * @param file the file entity
     * @return the file response DTO
     */
    private FileResponse mapToFileResponse(File file) {
        String taskUuid = null;
        String projectUuid = null;

        if (file.getTaskId() != null) {
            Task task = taskRepository.findById(file.getTaskId()).orElse(null);
            if (task != null && !task.getIsDeleted()) {
                taskUuid = task.getUuid();
            }
        }

        if (file.getProjectId() != null) {
            Project project = projectRepository.findById(file.getProjectId()).orElse(null);
            if (project != null && !project.getIsDeleted()) {
                projectUuid = project.getUuid();
            }
        }

        return FileResponse.builder()
                .uuid(file.getUuid())
                .fileName(file.getFileName())
                .filePath(file.getFilePath())
                .fileSize(file.getFileSize())
                .contentType(file.getContentType())
                .taskUuid(taskUuid)
                .projectUuid(projectUuid)
                .createdAt(file.getCreatedAt())
                .updatedAt(file.getUpdatedAt())
                .build();
    }
}