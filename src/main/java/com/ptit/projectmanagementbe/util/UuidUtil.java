package com.ptit.projectmanagementbe.util;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class UuidUtil {

    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    /**
     * Generate a random UUID
     *
     * @return UUID as string
     */
    public String generateUuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * Validate if a string is a valid UUID format
     *
     * @param uuid string to validate
     * @return true if valid UUID, false otherwise
     */
    public boolean isValidUuid(String uuid) {
        if (uuid == null) {
            return false;
        }
        return UUID_PATTERN.matcher(uuid).matches();
    }
}
