package com.ptit.projectmanagementbe.controller;

import com.ptit.projectmanagementbe.constant.ApiConstant;
import com.ptit.projectmanagementbe.constant.SuccessCode;
import com.ptit.projectmanagementbe.dto.common.ApiResponse;
import com.ptit.projectmanagementbe.dto.common.PageResponse;
import com.ptit.projectmanagementbe.dto.request.file.FileUploadRequest;
import com.ptit.projectmanagementbe.dto.response.file.FileResponse;
import com.ptit.projectmanagementbe.service.FileService;
import com.ptit.projectmanagementbe.util.MessageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

@RestController
@RequiredArgsConstructor
@Tag(name = "File", description = "File API")
public class FileUploadController {

    private final FileService fileService;
    private final MessageUtil messageUtil;

    @PostMapping(ApiConstant.UPLOAD_FILE)
    @Operation(summary = "Upload a file")
    public ResponseEntity<ApiResponse<FileResponse>> uploadFile(
            @Parameter(description = "File to upload", required = true)
            @RequestParam("file") MultipartFile file,
            Locale locale
    ) {
        FileResponse response = fileService.uploadFile(file, locale);
        String message = messageUtil.getMessage("file.uploaded", locale);
        return new ResponseEntity<>(
                ApiResponse.success(SuccessCode.FILE_UPLOADED, message, response),
                HttpStatus.CREATED
        );
    }

    @PostMapping(ApiConstant.FILE_BY_UUID + "/associate")
    @Operation(summary = "Associate a file with a task or project")
    public ResponseEntity<ApiResponse<FileResponse>> associateFile(
            @Parameter(description = "File UUID", required = true)
            @PathVariable(ApiConstant.UUID) String uuid,
            @Valid @RequestBody FileUploadRequest request,
            Locale locale
    ) {
        FileResponse response = fileService.associateFile(uuid, request, locale);
        String message = messageUtil.getMessage("file.associated", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.OK, message, response)
        );
    }

    @GetMapping(ApiConstant.FILE_BY_UUID)
    @Operation(summary = "Get a file by UUID")
    public ResponseEntity<ApiResponse<FileResponse>> getFileByUuid(
            @Parameter(description = "File UUID", required = true)
            @PathVariable(ApiConstant.UUID) String uuid,
            Locale locale
    ) {
        FileResponse response = fileService.getFileByUuid(uuid, locale);
        String message = messageUtil.getMessage("file.found", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.FILE_FOUND, message, response)
        );
    }

    @GetMapping(ApiConstant.FILE_BY_UUID + "/download")
    @Operation(summary = "Download a file by UUID")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "File UUID", required = true)
            @PathVariable(ApiConstant.UUID) String uuid,
            Locale locale
    ) {
        FileResponse fileInfo = fileService.getFileByUuid(uuid, locale);
        Resource resource = fileService.downloadFile(uuid, locale);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileInfo.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.getFileName() + "\"")
                .body(resource);
    }

    @GetMapping(ApiConstant.FILES + "/task/{taskUuid}")
    @Operation(summary = "Get all files for a task with pagination")
    public ResponseEntity<ApiResponse<PageResponse<FileResponse>>> getAllFilesByTask(
            @Parameter(description = "Task UUID", required = true)
            @PathVariable String taskUuid,
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
        PageResponse<FileResponse> response = fileService.getAllFilesByTask(taskUuid, pageable, locale);

        String message = messageUtil.getMessage("files.found", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.FILES_FOUND, message, response)
        );
    }

    @GetMapping(ApiConstant.FILES + "/project/{projectUuid}")
    @Operation(summary = "Get all files for a project with pagination")
    public ResponseEntity<ApiResponse<PageResponse<FileResponse>>> getAllFilesByProject(
            @Parameter(description = "Project UUID", required = true)
            @PathVariable String projectUuid,
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
        PageResponse<FileResponse> response = fileService.getAllFilesByProject(projectUuid, pageable, locale);

        String message = messageUtil.getMessage("files.found", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.FILES_FOUND, message, response)
        );
    }

    @GetMapping(ApiConstant.FILES + "/user/{userUuid}")
    @Operation(summary = "Get all files for a user with pagination")
    public ResponseEntity<ApiResponse<PageResponse<FileResponse>>> getAllFilesByUser(
            @Parameter(description = "User UUID", required = true)
            @PathVariable String userUuid,
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
        PageResponse<FileResponse> response = fileService.getAllFilesByUser(userUuid, pageable, locale);

        String message = messageUtil.getMessage("files.found", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.FILES_FOUND, message, response)
        );
    }

    @DeleteMapping(ApiConstant.FILE_BY_UUID)
    @Operation(summary = "Delete a file by UUID")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @Parameter(description = "File UUID", required = true)
            @PathVariable(ApiConstant.UUID) String uuid,
            Locale locale
    ) {
        fileService.deleteFile(uuid, locale);
        String message = messageUtil.getMessage("file.deleted", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.FILE_DELETED, message)
        );
    }
}
