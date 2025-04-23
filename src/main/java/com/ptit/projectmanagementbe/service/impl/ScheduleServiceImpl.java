package com.ptit.projectmanagementbe.service.impl;

import com.ptit.projectmanagementbe.constant.ErrorCode;
import com.ptit.projectmanagementbe.dto.common.PageResponse;
import com.ptit.projectmanagementbe.dto.request.schedule.ScheduleCreateRequest;
import com.ptit.projectmanagementbe.dto.request.schedule.ScheduleUpdateRequest;
import com.ptit.projectmanagementbe.dto.response.schedule.ScheduleResponse;
import com.ptit.projectmanagementbe.entity.Meeting;
import com.ptit.projectmanagementbe.entity.Schedule;
import com.ptit.projectmanagementbe.entity.Task;
import com.ptit.projectmanagementbe.entity.User;
import com.ptit.projectmanagementbe.exception.BadRequestException;
import com.ptit.projectmanagementbe.exception.ResourceNotFoundException;
import com.ptit.projectmanagementbe.repository.MeetingRepository;
import com.ptit.projectmanagementbe.repository.ScheduleRepository;
import com.ptit.projectmanagementbe.repository.TaskRepository;
import com.ptit.projectmanagementbe.repository.UserRepository;
import com.ptit.projectmanagementbe.service.ScheduleService;
import com.ptit.projectmanagementbe.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final MeetingRepository meetingRepository;
    private final MessageUtil messageUtil;

    // Default working hours (9 AM to 5 PM)
    private static final LocalTime DEFAULT_START_WORK_TIME = LocalTime.of(9, 0);
    private static final LocalTime DEFAULT_END_WORK_TIME = LocalTime.of(17, 0);

    @Override
    @Transactional
    public ScheduleResponse createSchedule(String userUuid, ScheduleCreateRequest request, Locale locale) {
        log.debug("Creating schedule for user with uuid: {}", userUuid);

        // Find the user
        User user = findUserByUuid(userUuid, locale);

        // Validate times
        if (request.getEndTime().isBefore(request.getStartTime()) || request.getEndTime().equals(request.getStartTime())) {
            throw new BadRequestException(
                    messageUtil.getMessage("schedule.invalid.date", locale),
                    ErrorCode.BAD_REQUEST
            );
        }

        // Check for time conflicts
        if (isScheduleTimeConflicting(userUuid, null, request.getScheduleDate(), request.getStartTime(), request.getEndTime())) {
            throw new BadRequestException(
                    messageUtil.getMessage("schedule.time.conflict", locale),
                    ErrorCode.SCHEDULE_TIME_CONFLICT
            );
        }

        // Create new schedule
        Schedule schedule = new Schedule();
        schedule.setScheduleDate(request.getScheduleDate());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setUserId(user.getId());

        // Associate with task or meeting if provided
        Integer taskId = null;
        Integer meetingId = null;
        String taskTitle = null;
        String meetingTitle = null;

        if (request.getTaskUuid() != null && !request.getTaskUuid().isEmpty()) {
            Task task = findTaskByUuid(request.getTaskUuid(), locale);
            taskId = task.getId();
            taskTitle = task.getTitle();
        }

        if (request.getMeetingUuid() != null && !request.getMeetingUuid().isEmpty()) {
            Meeting meeting = findMeetingByUuid(request.getMeetingUuid(), locale);
            meetingId = meeting.getId();
            meetingTitle = meeting.getTitle();
        }

        schedule.setTaskId(taskId);
        schedule.setMeetingId(meetingId);
        schedule.setCreatedBy(user.getName());
        schedule.setUpdatedBy(user.getName());

        Schedule savedSchedule = scheduleRepository.save(schedule);
        log.debug("Schedule created with id: {}", savedSchedule.getId());

        return mapToScheduleResponse(savedSchedule, request.getTaskUuid(), request.getMeetingUuid(), taskTitle, meetingTitle);
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleResponse getScheduleByUuid(String uuid, Locale locale) {
        log.debug("Getting schedule by uuid: {}", uuid);

        Schedule schedule = findScheduleByUuid(uuid, locale);

        String taskUuid = null;
        String meetingUuid = null;
        String taskTitle = null;
        String meetingTitle = null;

        if (schedule.getTaskId() != null) {
            Task task = taskRepository.findById(schedule.getTaskId()).orElse(null);
            if (task != null && !task.getIsDeleted()) {
                taskUuid = task.getUuid();
                taskTitle = task.getTitle();
            }
        }

        if (schedule.getMeetingId() != null) {
            Meeting meeting = meetingRepository.findById(schedule.getMeetingId()).orElse(null);
            if (meeting != null && !meeting.getIsDeleted()) {
                meetingUuid = meeting.getUuid();
                meetingTitle = meeting.getTitle();
            }
        }

        return mapToScheduleResponse(schedule, taskUuid, meetingUuid, taskTitle, meetingTitle);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ScheduleResponse> getAllSchedulesByUser(String userUuid, Pageable pageable, Locale locale) {
        log.debug("Getting all schedules for user with uuid: {}", userUuid);

        // Find the user
        User user = findUserByUuid(userUuid, locale);

        Page<Schedule> schedulePage = scheduleRepository.findByUserId(user.getId(), pageable);
        Page<ScheduleResponse> scheduleResponsePage = schedulePage.map(schedule -> {
            String taskUuid = null;
            String meetingUuid = null;
            String taskTitle = null;
            String meetingTitle = null;

            if (schedule.getTaskId() != null) {
                Task task = taskRepository.findById(schedule.getTaskId()).orElse(null);
                if (task != null && !task.getIsDeleted()) {
                    taskUuid = task.getUuid();
                    taskTitle = task.getTitle();
                }
            }

            if (schedule.getMeetingId() != null) {
                Meeting meeting = meetingRepository.findById(schedule.getMeetingId()).orElse(null);
                if (meeting != null && !meeting.getIsDeleted()) {
                    meetingUuid = meeting.getUuid();
                    meetingTitle = meeting.getTitle();
                }
            }

            return mapToScheduleResponse(schedule, taskUuid, meetingUuid, taskTitle, meetingTitle);
        });

        return PageResponse.fromPage(scheduleResponsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getAllSchedulesByUserAndDate(String userUuid, LocalDate date, Locale locale) {
        log.debug("Getting all schedules for user with uuid: {} on date: {}", userUuid, date);

        // Find the user
        User user = findUserByUuid(userUuid, locale);

        List<Schedule> schedules = scheduleRepository.findByUserIdAndScheduleDate(user.getId(), date);
        return schedules.stream().map(schedule -> {
            String taskUuid = null;
            String meetingUuid = null;
            String taskTitle = null;
            String meetingTitle = null;

            if (schedule.getTaskId() != null) {
                Task task = taskRepository.findById(schedule.getTaskId()).orElse(null);
                if (task != null && !task.getIsDeleted()) {
                    taskUuid = task.getUuid();
                    taskTitle = task.getTitle();
                }
            }

            if (schedule.getMeetingId() != null) {
                Meeting meeting = meetingRepository.findById(schedule.getMeetingId()).orElse(null);
                if (meeting != null && !meeting.getIsDeleted()) {
                    meetingUuid = meeting.getUuid();
                    meetingTitle = meeting.getTitle();
                }
            }

            return mapToScheduleResponse(schedule, taskUuid, meetingUuid, taskTitle, meetingTitle);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getAllSchedulesByUserAndDateBetween(String userUuid, LocalDate startDate, LocalDate endDate, Locale locale) {
        log.debug("Getting all schedules for user with uuid: {} between dates: {} and {}", userUuid, startDate, endDate);

        // Find the user
        User user = findUserByUuid(userUuid, locale);

        List<Schedule> schedules = scheduleRepository.findByUserIdAndScheduleDateBetween(user.getId(), startDate, endDate);
        return schedules.stream().map(schedule -> {
            String taskUuid = null;
            String meetingUuid = null;
            String taskTitle = null;
            String meetingTitle = null;

            if (schedule.getTaskId() != null) {
                Task task = taskRepository.findById(schedule.getTaskId()).orElse(null);
                if (task != null && !task.getIsDeleted()) {
                    taskUuid = task.getUuid();
                    taskTitle = task.getTitle();
                }
            }

            if (schedule.getMeetingId() != null) {
                Meeting meeting = meetingRepository.findById(schedule.getMeetingId()).orElse(null);
                if (meeting != null && !meeting.getIsDeleted()) {
                    meetingUuid = meeting.getUuid();
                    meetingTitle = meeting.getTitle();
                }
            }

            return mapToScheduleResponse(schedule, taskUuid, meetingUuid, taskTitle, meetingTitle);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ScheduleResponse updateSchedule(String uuid, ScheduleUpdateRequest request, Locale locale) {
        log.debug("Updating schedule with uuid: {}", uuid);

        Schedule schedule = findScheduleByUuid(uuid, locale);

        // Get values for validation
        LocalDate scheduleDate = request.getScheduleDate() != null ? request.getScheduleDate() : schedule.getScheduleDate();
        LocalTime startTime = request.getStartTime() != null ? request.getStartTime() : schedule.getStartTime();
        LocalTime endTime = request.getEndTime() != null ? request.getEndTime() : schedule.getEndTime();

        // Validate times
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            throw new BadRequestException(
                    messageUtil.getMessage("schedule.invalid.date", locale),
                    ErrorCode.BAD_REQUEST
            );
        }

        // Check for time conflicts if time or date is changing
        if ((request.getScheduleDate() != null || request.getStartTime() != null || request.getEndTime() != null) &&
                isScheduleTimeConflicting(null, schedule.getId(), scheduleDate, startTime, endTime)) {
            throw new BadRequestException(
                    messageUtil.getMessage("schedule.time.conflict", locale),
                    ErrorCode.SCHEDULE_TIME_CONFLICT
            );
        }

        // Update schedule fields if provided
        if (request.getScheduleDate() != null) {
            schedule.setScheduleDate(request.getScheduleDate());
        }
        if (request.getStartTime() != null) {
            schedule.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            schedule.setEndTime(request.getEndTime());
        }

        // Update task or meeting associations if provided
        Integer taskId = schedule.getTaskId();
        Integer meetingId = schedule.getMeetingId();
        String taskUuid = null;
        String meetingUuid = null;
        String taskTitle = null;
        String meetingTitle = null;

        if (request.getTaskUuid() != null) {
            if (request.getTaskUuid().isEmpty()) {
                // Remove task association
                taskId = null;
            } else {
                // Update task association
                Task task = findTaskByUuid(request.getTaskUuid(), locale);
                taskId = task.getId();
                taskUuid = task.getUuid();
                taskTitle = task.getTitle();
            }
        } else if (schedule.getTaskId() != null) {
            Task task = taskRepository.findById(schedule.getTaskId()).orElse(null);
            if (task != null && !task.getIsDeleted()) {
                taskUuid = task.getUuid();
                taskTitle = task.getTitle();
            }
        }

        if (request.getMeetingUuid() != null) {
            if (request.getMeetingUuid().isEmpty()) {
                // Remove meeting association
                meetingId = null;
            } else {
                // Update meeting association
                Meeting meeting = findMeetingByUuid(request.getMeetingUuid(), locale);
                meetingId = meeting.getId();
                meetingUuid = meeting.getUuid();
                meetingTitle = meeting.getTitle();
            }
        } else if (schedule.getMeetingId() != null) {
            Meeting meeting = meetingRepository.findById(schedule.getMeetingId()).orElse(null);
            if (meeting != null && !meeting.getIsDeleted()) {
                meetingUuid = meeting.getUuid();
                meetingTitle = meeting.getTitle();
            }
        }

        schedule.setTaskId(taskId);
        schedule.setMeetingId(meetingId);
        schedule.setUpdatedBy("system");

        Schedule updatedSchedule = scheduleRepository.save(schedule);
        log.debug("Schedule updated with id: {}", updatedSchedule.getId());

        return mapToScheduleResponse(updatedSchedule, taskUuid, meetingUuid, taskTitle, meetingTitle);
    }

    @Override
    @Transactional
    public void deleteSchedule(String uuid, Locale locale) {
        log.debug("Deleting schedule with uuid: {}", uuid);

        Schedule schedule = findScheduleByUuid(uuid, locale);
        schedule.setIsDeleted(true);
        schedule.setUpdatedBy("system");

        scheduleRepository.save(schedule);
        log.debug("Schedule deleted with id: {}", schedule.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalTime[]> findFreeTimeSlots(String userUuid, LocalDate date, int duration, Locale locale) {
        log.debug("Finding free time slots for user with uuid: {} on date: {} with duration: {} minutes", userUuid, date, duration);

        // Find the user
        User user = findUserByUuid(userUuid, locale);

        // Get all schedules for the user on the specified date
        List<Schedule> schedules = scheduleRepository.findByUserIdAndScheduleDate(user.getId(), date);

        // Sort schedules by start time
        schedules.sort(Comparator.comparing(Schedule::getStartTime));

        // Initialize result list
        List<LocalTime[]> freeTimeSlots = new ArrayList<>();

        // Generate time slots
        LocalTime currentTime = DEFAULT_START_WORK_TIME;

        for (Schedule schedule : schedules) {
            // If there's a gap before the current schedule
            if (currentTime.isBefore(schedule.getStartTime())) {
                // Check if the gap is long enough for the required duration
                long minutesGap = ChronoUnit.MINUTES.between(currentTime, schedule.getStartTime());
                if (minutesGap >= duration) {
                    freeTimeSlots.add(new LocalTime[]{currentTime, schedule.getStartTime()});
                }
            }

            // Update current time to end of this schedule
            currentTime = schedule.getEndTime();
        }

        // Check if there's time left until end of workday
        if (currentTime.isBefore(DEFAULT_END_WORK_TIME)) {
            long minutesGap = ChronoUnit.MINUTES.between(currentTime, DEFAULT_END_WORK_TIME);
            if (minutesGap >= duration) {
                freeTimeSlots.add(new LocalTime[]{currentTime, DEFAULT_END_WORK_TIME});
            }
        }

        return freeTimeSlots;
    }

    @Override
    public boolean isScheduleTimeConflicting(String userUuid, Integer scheduleId, LocalDate date,
                                             LocalTime startTime, LocalTime endTime) {
        Integer userId = null;
        if (userUuid != null) {
            User user = userRepository.findActiveByUuid(userUuid).orElse(null);
            if (user != null) {
                userId = user.getId();
            }
        }

        if (userId == null && scheduleId != null) {
            Schedule schedule = scheduleRepository.findById(scheduleId).orElse(null);
            if (schedule != null) {
                userId = schedule.getUserId();
            }
        }

        if (userId == null) {
            return false;
        }

        Integer excludeId = scheduleId != null ? scheduleId : 0;
        return scheduleRepository.existsOverlappingSchedule(userId, date, startTime, endTime, excludeId);
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
     * Find a schedule by uuid
     *
     * @param uuid   the schedule uuid
     * @param locale the current locale
     * @return the schedule entity
     * @throws ResourceNotFoundException if the schedule is not found
     */
    private Schedule findScheduleByUuid(String uuid, Locale locale) {
        return scheduleRepository.findActiveByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("schedule.not.found", locale, "uuid", uuid),
                        "Schedule", "uuid", uuid
                ));
    }

    /**
     * Map a Schedule entity to a ScheduleResponse DTO
     *
     * @param schedule     the schedule entity
     * @param taskUuid     the associated task uuid
     * @param meetingUuid  the associated meeting uuid
     * @param taskTitle    the associated task title
     * @param meetingTitle the associated meeting title
     * @return the schedule response DTO
     */
    private ScheduleResponse mapToScheduleResponse(Schedule schedule, String taskUuid, String meetingUuid,
                                                   String taskTitle, String meetingTitle) {
        return ScheduleResponse.builder()
                .uuid(schedule.getUuid())
                .scheduleDate(schedule.getScheduleDate())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .taskUuid(taskUuid)
                .meetingUuid(meetingUuid)
                .taskTitle(taskTitle)
                .meetingTitle(meetingTitle)
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }
}
