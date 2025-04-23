package com.ptit.projectmanagementbe.service.impl;


import com.ptit.projectmanagementbe.constant.ErrorCode;
import com.ptit.projectmanagementbe.dto.common.PageResponse;
import com.ptit.projectmanagementbe.dto.request.user.UserCreateRequest;
import com.ptit.projectmanagementbe.dto.request.user.UserUpdateRequest;
import com.ptit.projectmanagementbe.dto.response.user.UserResponse;
import com.ptit.projectmanagementbe.entity.User;
import com.ptit.projectmanagementbe.exception.BadRequestException;
import com.ptit.projectmanagementbe.exception.ResourceNotFoundException;
import com.ptit.projectmanagementbe.repository.UserRepository;
import com.ptit.projectmanagementbe.service.UserService;
import com.ptit.projectmanagementbe.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MessageUtil messageUtil;

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request, Locale locale) {
        log.debug("Creating user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(
                    messageUtil.getMessage("user.email.exists", locale, request.getEmail()),
                    ErrorCode.USER_ALREADY_EXISTS,
                    request.getEmail()
            );
        }

        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        // In a real application, the password would be encrypted
        user.setPassword(request.getPassword());
        user.setReminderSettings(request.getReminderSettings());

        User savedUser = userRepository.save(user);
        log.debug("User created with id: {}", savedUser.getId());

        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUuid(String uuid, Locale locale) {
        log.debug("Getting user by uuid: {}", uuid);

        User user = findUserByUuid(uuid, locale);
        return mapToUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAllUsers(Pageable pageable, Locale locale) {
        log.debug("Getting all users with pageable: {}", pageable);

        Page<User> userPage = userRepository.findAll(pageable);
        Page<UserResponse> userResponsePage = userPage.map(this::mapToUserResponse);

        return PageResponse.fromPage(userResponsePage);
    }

    @Override
    @Transactional
    public UserResponse updateUser(String uuid, UserUpdateRequest request, Locale locale) {
        log.debug("Updating user with uuid: {}", uuid);

        User user = findUserByUuid(uuid, locale);

        // Check if email is being changed and already exists
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(
                    messageUtil.getMessage("user.email.exists", locale, request.getEmail()),
                    ErrorCode.USER_ALREADY_EXISTS,
                    request.getEmail()
            );
        }

        // Update user fields if provided
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null) {
            // In a real application, the password would be encrypted
            user.setPassword(request.getPassword());
        }
        if (request.getReminderSettings() != null) {
            user.setReminderSettings(request.getReminderSettings());
        }

        User updatedUser = userRepository.save(user);
        log.debug("User updated with id: {}", updatedUser.getId());

        return mapToUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(String uuid, Locale locale) {
        log.debug("Deleting user with uuid: {}", uuid);

        User user = findUserByUuid(uuid, locale);
        user.setIsDeleted(true);

        userRepository.save(user);
        log.debug("User deleted with id: {}", user.getId());
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
     * Map a User entity to a UserResponse DTO
     *
     * @param user the user entity
     * @return the user response DTO
     */
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .uuid(user.getUuid())
                .name(user.getName())
                .email(user.getEmail())
                .reminderSettings(user.getReminderSettings())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
