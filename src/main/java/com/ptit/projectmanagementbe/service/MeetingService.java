package com.ptit.projectmanagementbe.service;

import com.ptit.projectmanagementbe.dto.common.PageResponse;
import com.ptit.projectmanagementbe.dto.request.meeting.MeetingCreateRequest;
import com.ptit.projectmanagementbe.dto.request.meeting.MeetingUpdateRequest;
import com.ptit.projectmanagementbe.dto.response.meeting.MeetingResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public interface MeetingService {

    /**
     * Create a new meeting for a user
     *
     * @param userUuid the user uuid
     * @param request  the meeting create request
     * @param locale   the current locale
     * @return the created meeting response
     */
    MeetingResponse createMeeting(String userUuid, MeetingCreateRequest request, Locale locale);

    /**
     * Get a meeting by uuid
     *
     * @param uuid   the meeting uuid
     * @param locale the current locale
     * @return the meeting response
     */
    MeetingResponse getMeetingByUuid(String uuid, Locale locale);

    /**
     * Get all meetings for a user with pagination
     *
     * @param userUuid the user uuid
     * @param pageable the pageable information
     * @param locale   the current locale
     * @return the page of meeting responses
     */
    PageResponse<MeetingResponse> getAllMeetingsByUser(String userUuid, Pageable pageable, Locale locale);

    /**
     * Get all meetings for a user on a specific date
     *
     * @param userUuid the user uuid
     * @param date     the meeting date
     * @param locale   the current locale
     * @return the list of meeting responses
     */
    List<MeetingResponse> getAllMeetingsByUserAndDate(String userUuid, LocalDate date, Locale locale);

    /**
     * Get all meetings for a user between two dates
     *
     * @param userUuid  the user uuid
     * @param startDate the start date
     * @param endDate   the end date
     * @param locale    the current locale
     * @return the list of meeting responses
     */
    List<MeetingResponse> getAllMeetingsByUserAndDateBetween(String userUuid, LocalDate startDate, LocalDate endDate, Locale locale);

    /**
     * Update a meeting by uuid
     *
     * @param uuid    the meeting uuid
     * @param request the meeting update request
     * @param locale  the current locale
     * @return the updated meeting response
     */
    MeetingResponse updateMeeting(String uuid, MeetingUpdateRequest request, Locale locale);

    /**
     * Delete a meeting by uuid (soft delete)
     *
     * @param uuid   the meeting uuid
     * @param locale the current locale
     */
    void deleteMeeting(String uuid, Locale locale);

    /**
     * Check if a meeting time conflicts with existing meetings
     *
     * @param userUuid  the user uuid
     * @param meetingId the meeting id to exclude (can be null for new meetings)
     * @param date      the meeting date
     * @param startTime the start time
     * @param endTime   the end time
     * @return true if there's a conflict, false otherwise
     */
    boolean isMeetingTimeConflicting(String userUuid, Integer meetingId, LocalDate date,
                                     java.time.LocalTime startTime, java.time.LocalTime endTime);
}
