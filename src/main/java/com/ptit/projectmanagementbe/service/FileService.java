package com.ptit.projectmanagementbe.service;

import com.ptit.projectmanagementbe.dto.common.PageResponse;
import com.ptit.projectmanagementbe.dto.request.file.FileUploadRequest;
import com.ptit.projectmanagementbe.dto.response.file.FileResponse;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

public interface FileService {

    /**
     * Upload a file
     *
     * @param file   the multipart file
     * @param locale the current locale
     * @return the uploaded file response
     */
    FileResponse uploadFile(MultipartFile file, Locale locale);

    /**
     * Associate a file with a task or project
     *
     * @param fileUuid the file uuid
     * @param request  the file upload request
     * @param locale   the current locale
     * @return the updated file response
     */
    FileResponse associateFile(String fileUuid, FileUploadRequest request, Locale locale);

    /**
     * Get a file by uuid
     *
     * @param uuid   the file uuid
     * @param locale the current locale
     * @return the file response
     */
    FileResponse getFileByUuid(String uuid, Locale locale);

    /**
     * Download a file by uuid
     *
     * @param uuid   the file uuid
     * @param locale the current locale
     * @return the file resource
     */
    Resource downloadFile(String uuid, Locale locale);

    /**
     * Get all files for a task with pagination
     *
     * @param taskUuid the task uuid
     * @param pageable the pageable information
     * @param locale   the current locale
     * @return the page of file responses
     */
    PageResponse<FileResponse> getAllFilesByTask(String taskUuid, Pageable pageable, Locale locale);

    /**
     * Get all files for a project with pagination
     *
     * @param projectUuid the project uuid
     * @param pageable    the pageable information
     * @param locale      the current locale
     * @return the page of file responses
     */
    PageResponse<FileResponse> getAllFilesByProject(String projectUuid, Pageable pageable, Locale locale);

    /**
     * Get all files for a user with pagination
     *
     * @param userUuid the user uuid
     * @param pageable the pageable information
     * @param locale   the current locale
     * @return the page of file responses
     */
    PageResponse<FileResponse> getAllFilesByUser(String userUuid, Pageable pageable, Locale locale);

    /**
     * Delete a file by uuid (soft delete)
     *
     * @param uuid   the file uuid
     * @param locale the current locale
     */
    void deleteFile(String uuid, Locale locale);
}
