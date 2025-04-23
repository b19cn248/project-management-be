package com.ptit.projectmanagementbe.service;

import com.ptit.projectmanagementbe.dto.common.PageResponse;
import com.ptit.projectmanagementbe.dto.request.user.UserCreateRequest;
import com.ptit.projectmanagementbe.dto.request.user.UserUpdateRequest;
import com.ptit.projectmanagementbe.dto.response.user.UserResponse;
import org.springframework.data.domain.Pageable;

import java.util.Locale;

public interface UserService {

    /**
     * Create a new user
     *
     * @param request the user create request
     * @param locale  the current locale
     * @return the created user response
     */
    UserResponse createUser(UserCreateRequest request, Locale locale);

    /**
     * Get a user by uuid
     *
     * @param uuid   the user uuid
     * @param locale the current locale
     * @return the user response
     */
    UserResponse getUserByUuid(String uuid, Locale locale);

    /**
     * Get all users with pagination
     *
     * @param pageable the pageable information
     * @param locale   the current locale
     * @return the page of user responses
     */
    PageResponse<UserResponse> getAllUsers(Pageable pageable, Locale locale);

    /**
     * Update a user by uuid
     *
     * @param uuid    the user uuid
     * @param request the user update request
     * @param locale  the current locale
     * @return the updated user response
     */
    UserResponse updateUser(String uuid, UserUpdateRequest request, Locale locale);

    /**
     * Delete a user by uuid (soft delete)
     *
     * @param uuid   the user uuid
     * @param locale the current locale
     */
    void deleteUser(String uuid, Locale locale);
}
