package com.ptit.projectmanagementbe.service.impl;

import com.ptit.projectmanagementbe.constant.ErrorCode;
import com.ptit.projectmanagementbe.dto.common.PageResponse;
import com.ptit.projectmanagementbe.dto.request.meeting.MeetingCreateRequest;
import com.ptit.projectmanagementbe.dto.request.meeting.MeetingUpdateRequest;
import com.ptit.projectmanagementbe.dto.response.meeting.MeetingResponse;
import com.ptit.projectmanagementbe.entity.Meeting;
import com.ptit.projectmanagementbe.entity.User;
import com.ptit.projectmanagementbe.exception.BadRequestException;
import com.ptit.projectmanagementbe.exception.ResourceNotFoundException;
import com.ptit.projectmanagementbe.repository.MeetingRepository;
import com.ptit.projectmanagementbe.repository.UserRepository;
import com.ptit.projectmanagementbe.service.MeetingService;
import com.ptit.projectmanagementbe.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final MessageUtil messageUtil;

    @Override
    @Transactional
    public MeetingResponse createMeeting(String userUuid, MeetingCreateRequest request, Locale locale) {
        log.debug("Creating meeting for user with uuid: {}", userUuid);

        // Find the user
        User user = findUserByUuid(userUuid, locale);

        // Validate times
        if (request.getEndTime().isBefore(request.getStartTime()) || request.getEndTime().equals(request.getStartTime())) {
            throw new BadRequestException(
                    messageUtil.getMessage("meeting.invalid.time", locale),
                    ErrorCode.BAD_REQUEST
            );
        }

        // Check for time conflicts
        if (isMeetingTimeConflicting(userUuid, null, request.getMeetingDate(), request.getStartTime(), request.getEndTime())) {
            throw new BadRequestException(
                    messageUtil.getMessage("meeting.time.conflict", locale),
                    ErrorCode.MEETING_TIME_CONFLICT
            );
        }

        // Create new meeting
        Meeting meeting = new Meeting();
        meeting.setTitle(request.getTitle());
        meeting.setDescription(request.getDescription());
        meeting.setMeetingDate(request.getMeetingDate());
        meeting.setStartTime(request.getStartTime());
        meeting.setEndTime(request.getEndTime());
        meeting.setParticipants(request.getParticipants());
        meeting.setUserId(user.getId());
        meeting.setCreatedBy(user.getName());
        meeting.setUpdatedBy(user.getName());

        Meeting savedMeeting = meetingRepository.save(meeting);
        log.debug("Meeting created with id: {}", savedMeeting.getId());

        return mapToMeetingResponse(savedMeeting);
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingResponse getMeetingByUuid(String uuid, Locale locale) {
        log.debug("Getting meeting by uuid: {}", uuid);

        Meeting meeting = findMeetingByUuid(uuid, locale);
        return mapToMeetingResponse(meeting);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MeetingResponse> getAllMeetingsByUser(String userUuid, Pageable pageable, Locale locale) {
        log.debug("Getting all meetings for user with uuid: {}", userUuid);

        // Find the user
        User user = findUserByUuid(userUuid, locale);

        Page<Meeting> meetingPage = meetingRepository.findByUserId(user.getId(), pageable);
        Page<MeetingResponse> meetingResponsePage = meetingPage.map(this::mapToMeetingResponse);

        return PageResponse.fromPage(meetingResponsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingResponse> getAllMeetingsByUserAndDate(String userUuid, LocalDate date, Locale locale) {
        log.debug("Getting all meetings for user with uuid: {} on date: {}", userUuid, date);

        // Find the user
        User user = findUserByUuid(userUuid, locale);

        List<Meeting> meetings = meetingRepository.findByUserIdAndMeetingDate(user.getId(), date);
        return meetings.stream().map(this::mapToMeetingResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingResponse> getAllMeetingsByUserAndDateBetween(String userUuid, LocalDate startDate, LocalDate endDate, Locale locale) {
        log.debug("Getting all meetings for user with uuid: {} between dates: {} and {}", userUuid, startDate, endDate);

        // Find the user
        User user = findUserByUuid(userUuid, locale);

        List<Meeting> meetings = meetingRepository.findByUserIdAndMeetingDateBetween(user.getId(), startDate, endDate);
        return meetings.stream().map(this::mapToMeetingResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MeetingResponse updateMeeting(String uuid, MeetingUpdateRequest request, Locale locale) {
        log.debug("Updating meeting with uuid: {}", uuid);

        Meeting meeting = findMeetingByUuid(uuid, locale);

        // Get values for validation
        LocalDate meetingDate = request.getMeetingDate() != null ? request.getMeetingDate() : meeting.getMeetingDate();
        LocalTime startTime = request.getStartTime() != null ? request.getStartTime() : meeting.getStartTime();
        LocalTime endTime = request.getEndTime() != null ? request.getEndTime() : meeting.getEndTime();

        // Validate times
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            throw new BadRequestException(
                    messageUtil.getMessage("meeting.invalid.time", locale),
                    ErrorCode.BAD_REQUEST
            );
        }

        // Check for time conflicts if time or date is changing
        if ((request.getMeetingDate() != null || request.getStartTime() != null || request.getEndTime() != null) &&
                isMeetingTimeConflicting(null, meeting.getId(), meetingDate, startTime, endTime)) {
            throw new BadRequestException(
                    messageUtil.getMessage("meeting.time.conflict", locale),
                    ErrorCode.MEETING_TIME_CONFLICT
            );
        }

        // Update meeting fields if provided
        if (request.getTitle() != null) {
            meeting.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            meeting.setDescription(request.getDescription());
        }
        if (request.getMeetingDate() != null) {
            meeting.setMeetingDate(request.getMeetingDate());
        }
        if (request.getStartTime() != null) {
            meeting.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            meeting.setEndTime(request.getEndTime());
        }
        if (request.getParticipants() != null) {
            meeting.setParticipants(request.getParticipants());
        }
        meeting.setUpdatedBy("system");

        Meeting updatedMeeting = meetingRepository.save(meeting);
        log.debug("Meeting updated with id: {}", updatedMeeting.getId());

        return mapToMeetingResponse(updatedMeeting);
    }

    @Override
    @Transactional
    public void deleteMeeting(String uuid, Locale locale) {
        log.debug("Deleting meeting with uuid: {}", uuid);

        Meeting meeting = findMeetingByUuid(uuid, locale);
        meeting.setIsDeleted(true);
        meeting.setUpdatedBy("system");

        meetingRepository.save(meeting);
        log.debug("Meeting deleted with id: {}", meeting.getId());
    }

    @Override
    public boolean isMeetingTimeConflicting(String userUuid, Integer meetingId, LocalDate date,
                                            LocalTime startTime, LocalTime endTime) {
        Integer userId = null;
        if (userUuid != null) {
            User user = userRepository.findActiveByUuid(userUuid).orElse(null);
            if (user != null) {
                userId = user.getId();
            }
        }

        if (userId == null && meetingId != null) {
            Meeting meeting = meetingRepository.findById(meetingId).orElse(null);
            if (meeting != null) {
                userId = meeting.getUserId();
            }
        }

        if (userId == null) {
            return false;
        }

        Integer excludeId = meetingId != null ? meetingId : 0;
        return meetingRepository.existsOverlappingMeeting(userId, date, startTime, endTime, excludeId);
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
     * Find a meeting by uuid
     *
     * @param uuid   the meeting uuid
     * @param locale the current locale
     * @return the meeting entity
     * @throws ResourceNotFoundException if the meeting is not found
     */
    private Meeting findMeetingByUuid(String uuid, Locale locale) {
        return meetingRepository.findActiveByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("meeting.not.found", locale, "uuid", uuid),
                        "Meeting", "uuid", uuid
                ));
    }

    /**
     * Map a Meeting entity to a MeetingResponse DTO
     *
     * @param meeting the meeting entity
     * @return the meeting response DTO
     */
    private MeetingResponse mapToMeetingResponse(Meeting meeting) {
        return MeetingResponse.builder()
                .uuid(meeting.getUuid())
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .meetingDate(meeting.getMeetingDate())
                .startTime(meeting.getStartTime())
                .endTime(meeting.getEndTime())
                .participants(meeting.getParticipants())
                .createdAt(meeting.getCreatedAt())
                .updatedAt(meeting.getUpdatedAt())
                .build();
    }
}
