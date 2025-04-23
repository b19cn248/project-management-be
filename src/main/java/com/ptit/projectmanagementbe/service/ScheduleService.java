package com.ptit.projectmanagementbe.service;

import com.ptit.projectmanagementbe.dto.common.PageResponse;
import com.ptit.projectmanagementbe.dto.request.schedule.ScheduleCreateRequest;
import com.ptit.projectmanagementbe.dto.request.schedule.ScheduleUpdateRequest;
import com.ptit.projectmanagementbe.dto.response.schedule.ScheduleResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

public interface ScheduleService {

    /**
     * Create a new schedule for a user
     *
     * @param userUuid the user uuid
     * @param request  the schedule create request
     * @param locale   the current locale
     * @return the created schedule response
     */
    ScheduleResponse createSchedule(String userUuid, ScheduleCreateRequest request, Locale locale);

    /**
     * Get a schedule by uuid
     *
     * @param uuid   the schedule uuid
     * @param locale the current locale
     * @return the schedule response
     */
    ScheduleResponse getScheduleByUuid(String uuid, Locale locale);

    /**
     * Get all schedules for a user with pagination
     *
     * @param userUuid the user uuid
     * @param pageable the pageable information
     * @param locale   the current locale
     * @return the page of schedule responses
     */
    PageResponse<ScheduleResponse> getAllSchedulesByUser(String userUuid, Pageable pageable, Locale locale);

    /**
     * Get all schedules for a user on a specific date
     *
     * @param userUuid the user uuid
     * @param date     the schedule date
     * @param locale   the current locale
     * @return the list of schedule responses
     */
    List<ScheduleResponse> getAllSchedulesByUserAndDate(String userUuid, LocalDate date, Locale locale);

    /**
     * Get all schedules for a user between two dates
     *
     * @param userUuid  the user uuid
     * @param startDate the start date
     * @param endDate   the end date
     * @param locale    the current locale
     * @return the list of schedule responses
     */
    List<ScheduleResponse> getAllSchedulesByUserAndDateBetween(String userUuid, LocalDate startDate, LocalDate endDate, Locale locale);

    /**
     * Update a schedule by uuid
     *
     * @param uuid    the schedule uuid
     * @param request the schedule update request
     * @param locale  the current locale
     * @return the updated schedule response
     */
    ScheduleResponse updateSchedule(String uuid, ScheduleUpdateRequest request, Locale locale);

    /**
     * Delete a schedule by uuid (soft delete)
     *
     * @param uuid   the schedule uuid
     * @param locale the current locale
     */
    void deleteSchedule(String uuid, Locale locale);

    /**
     * Find free time slots in a user's schedule for a specific date
     *
     * @param userUuid the user uuid
     * @param date     the date to check
     * @param duration the required duration in minutes
     * @param locale   the current locale
     * @return the list of available time slots (start time, end time)
     */
    List<LocalTime[]> findFreeTimeSlots(String userUuid, LocalDate date, int duration, Locale locale);

    /**
     * Check if a schedule time conflicts with existing schedules
     *
     * @param userUuid   the user uuid
     * @param scheduleId the schedule id to exclude (can be null for new schedules)
     * @param date       the schedule date
     * @param startTime  the start time
     * @param endTime    the end time
     * @return true if there's a conflict, false otherwise
     */
    boolean isScheduleTimeConflicting(String userUuid, Integer scheduleId, LocalDate date,
                                      LocalTime startTime, LocalTime endTime);
}
