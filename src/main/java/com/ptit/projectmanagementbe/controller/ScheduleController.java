package com.ptit.projectmanagementbe.controller;

import com.ptit.projectmanagementbe.constant.ApiConstant;
import com.ptit.projectmanagementbe.constant.SuccessCode;
import com.ptit.projectmanagementbe.dto.common.ApiResponse;
import com.ptit.projectmanagementbe.dto.common.PageResponse;
import com.ptit.projectmanagementbe.dto.request.schedule.ScheduleCreateRequest;
import com.ptit.projectmanagementbe.dto.request.schedule.ScheduleUpdateRequest;
import com.ptit.projectmanagementbe.dto.response.schedule.ScheduleResponse;
import com.ptit.projectmanagementbe.service.ScheduleService;
import com.ptit.projectmanagementbe.util.MessageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Tag(name = "Schedule", description = "Schedule API")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final MessageUtil messageUtil;

    @PostMapping(ApiConstant.SCHEDULES)
    @Operation(summary = "Create a new schedule for a user")
    public ResponseEntity<ApiResponse<ScheduleResponse>> createSchedule(
            @Parameter(description = "User UUID", required = true)
            @RequestParam String userUuid,
            @Valid @RequestBody ScheduleCreateRequest request,
            Locale locale
    ) {
        ScheduleResponse response = scheduleService.createSchedule(userUuid, request, locale);
        String message = messageUtil.getMessage("schedule.created", locale);
        return new ResponseEntity<>(
                ApiResponse.success(SuccessCode.SCHEDULE_CREATED, message, response),
                HttpStatus.CREATED
        );
    }

    @GetMapping(ApiConstant.SCHEDULE_BY_UUID)
    @Operation(summary = "Get a schedule by UUID")
    public ResponseEntity<ApiResponse<ScheduleResponse>> getScheduleByUuid(
            @Parameter(description = "Schedule UUID", required = true)
            @PathVariable(ApiConstant.UUID) String uuid,
            Locale locale
    ) {
        ScheduleResponse response = scheduleService.getScheduleByUuid(uuid, locale);
        String message = messageUtil.getMessage("schedule.found", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.SCHEDULE_FOUND, message, response)
        );
    }

    @GetMapping(ApiConstant.SCHEDULES)
    @Operation(summary = "Get all schedules for a user with pagination")
    public ResponseEntity<ApiResponse<PageResponse<ScheduleResponse>>> getAllSchedulesByUser(
            @Parameter(description = "User UUID", required = true)
            @RequestParam String userUuid,
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
        PageResponse<ScheduleResponse> response = scheduleService.getAllSchedulesByUser(userUuid, pageable, locale);

        String message = messageUtil.getMessage("schedules.found", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.SCHEDULES_FOUND, message, response)
        );
    }

    @GetMapping(ApiConstant.SCHEDULES + "/date")
    @Operation(summary = "Get all schedules for a user on a specific date")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getAllSchedulesByUserAndDate(
            @Parameter(description = "User UUID", required = true)
            @RequestParam String userUuid,
            @Parameter(description = "Schedule date (format: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Locale locale
    ) {
        List<ScheduleResponse> response = scheduleService.getAllSchedulesByUserAndDate(userUuid, date, locale);
        String message = messageUtil.getMessage("schedules.found", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.SCHEDULES_FOUND, message, response)
        );
    }

    @GetMapping(ApiConstant.SCHEDULES + "/between")
    @Operation(summary = "Get all schedules for a user between two dates")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getAllSchedulesByUserAndDateBetween(
            @Parameter(description = "User UUID", required = true)
            @RequestParam String userUuid,
            @Parameter(description = "Start date (format: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (format: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Locale locale
    ) {
        List<ScheduleResponse> response = scheduleService.getAllSchedulesByUserAndDateBetween(userUuid, startDate, endDate, locale);
        String message = messageUtil.getMessage("schedules.found", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.SCHEDULES_FOUND, message, response)
        );
    }

    @GetMapping(ApiConstant.SCHEDULES + "/free-slots")
    @Operation(summary = "Find free time slots in a user's schedule for a specific date")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> findFreeTimeSlots(
            @Parameter(description = "User UUID", required = true)
            @RequestParam String userUuid,
            @Parameter(description = "Date (format: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Required duration in minutes", required = true)
            @RequestParam int duration,
            Locale locale
    ) {
        List<LocalTime[]> freeTimeSlots = scheduleService.findFreeTimeSlots(userUuid, date, duration, locale);

        // Convert to a more user-friendly format
        List<Map<String, String>> formattedTimeSlots = freeTimeSlots.stream()
                .map(slot -> {
                    Map<String, String> timeSlot = new HashMap<>();
                    timeSlot.put("startTime", slot[0].toString());
                    timeSlot.put("endTime", slot[1].toString());
                    return timeSlot;
                })
                .collect(Collectors.toList());

        String message = messageUtil.getMessage("free.slots.found", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.OK, message, formattedTimeSlots)
        );
    }

    @PutMapping(ApiConstant.SCHEDULE_BY_UUID)
    @Operation(summary = "Update a schedule by UUID")
    public ResponseEntity<ApiResponse<ScheduleResponse>> updateSchedule(
            @Parameter(description = "Schedule UUID", required = true)
            @PathVariable(ApiConstant.UUID) String uuid,
            @Valid @RequestBody ScheduleUpdateRequest request,
            Locale locale
    ) {
        ScheduleResponse response = scheduleService.updateSchedule(uuid, request, locale);
        String message = messageUtil.getMessage("schedule.updated", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.SCHEDULE_UPDATED, message, response)
        );
    }

    @DeleteMapping(ApiConstant.SCHEDULE_BY_UUID)
    @Operation(summary = "Delete a schedule by UUID")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(
            @Parameter(description = "Schedule UUID", required = true)
            @PathVariable(ApiConstant.UUID) String uuid,
            Locale locale
    ) {
        scheduleService.deleteSchedule(uuid, locale);
        String message = messageUtil.getMessage("schedule.deleted", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.SCHEDULE_DELETED, message)
        );
    }
}
