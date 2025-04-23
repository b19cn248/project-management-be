package com.ptit.projectmanagementbe.controller;

import com.ptit.projectmanagementbe.constant.ApiConstant;
import com.ptit.projectmanagementbe.constant.SuccessCode;
import com.ptit.projectmanagementbe.dto.common.ApiResponse;
import com.ptit.projectmanagementbe.dto.common.PageResponse;
import com.ptit.projectmanagementbe.dto.request.user.UserCreateRequest;
import com.ptit.projectmanagementbe.dto.request.user.UserUpdateRequest;
import com.ptit.projectmanagementbe.dto.response.user.UserResponse;
import com.ptit.projectmanagementbe.service.UserService;
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

import java.util.Locale;

@RestController
@RequiredArgsConstructor
@Tag(name = "User", description = "User API")
public class UserController {

    private final UserService userService;
    private final MessageUtil messageUtil;

    @PostMapping(ApiConstant.USERS)
    @Operation(summary = "Create a new user")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request,
            Locale locale
    ) {
        UserResponse response = userService.createUser(request, locale);
        String message = messageUtil.getMessage("user.created", locale);
        return new ResponseEntity<>(
                ApiResponse.success(SuccessCode.USER_CREATED, message, response),
                HttpStatus.CREATED
        );
    }

    @GetMapping(ApiConstant.USER_BY_UUID)
    @Operation(summary = "Get a user by UUID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUuid(
            @Parameter(description = "User UUID", required = true)
            @PathVariable(ApiConstant.UUID) String uuid,
            Locale locale
    ) {
        UserResponse response = userService.getUserByUuid(uuid, locale);
        String message = messageUtil.getMessage("user.found", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.USER_FOUND, message, response)
        );
    }

    @GetMapping(ApiConstant.USERS)
    @Operation(summary = "Get all users with pagination")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
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
        PageResponse<UserResponse> response = userService.getAllUsers(pageable, locale);

        String message = messageUtil.getMessage("users.found", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.USERS_FOUND, message, response)
        );
    }

    @PutMapping(ApiConstant.USER_BY_UUID)
    @Operation(summary = "Update a user by UUID")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @Parameter(description = "User UUID", required = true)
            @PathVariable(ApiConstant.UUID) String uuid,
            @Valid @RequestBody UserUpdateRequest request,
            Locale locale
    ) {
        UserResponse response = userService.updateUser(uuid, request, locale);
        String message = messageUtil.getMessage("user.updated", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.USER_UPDATED, message, response)
        );
    }

    @DeleteMapping(ApiConstant.USER_BY_UUID)
    @Operation(summary = "Delete a user by UUID")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "User UUID", required = true)
            @PathVariable(ApiConstant.UUID) String uuid,
            Locale locale
    ) {
        userService.deleteUser(uuid, locale);
        String message = messageUtil.getMessage("user.deleted", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.USER_DELETED, message)
        );
    }
}
