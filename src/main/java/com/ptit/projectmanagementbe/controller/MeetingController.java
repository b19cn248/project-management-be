package com.ptit.projectmanagementbe.controller;

import com.ptit.projectmanagementbe.constant.ApiConstant;
import com.ptit.projectmanagementbe.constant.SuccessCode;
import com.ptit.projectmanagementbe.dto.common.ApiResponse;
import com.ptit.projectmanagementbe.dto.common.PageResponse;
import com.ptit.projectmanagementbe.dto.request.meeting.MeetingCreateRequest;
import com.ptit.projectmanagementbe.dto.request.meeting.MeetingUpdateRequest;
import com.ptit.projectmanagementbe.dto.response.meeting.MeetingResponse;
import com.ptit.projectmanagementbe.service.MeetingService;
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
import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
@Tag(name = "Meeting", description = "Meeting API")
public class MeetingController {

    private final MeetingService meetingService;
    private final MessageUtil messageUtil;

    @PostMapping(ApiConstant.MEETINGS)
    @Operation(summary = "Create a new meeting for a user")
    public ResponseEntity<ApiResponse<MeetingResponse>> createMeeting(
            @Parameter(description = "User UUID", required = true)
            @RequestParam String userUuid,
            @Valid @RequestBody MeetingCreateRequest request,
            Locale locale
    ) {
        MeetingResponse response = meetingService.createMeeting(userUuid, request, locale);
        String message = messageUtil.getMessage("meeting.created", locale);
        return new ResponseEntity<>(
                ApiResponse.success(SuccessCode.MEETING_CREATED, message, response),
                HttpStatus.CREATED
        );
    }

    @GetMapping(ApiConstant.MEETING_BY_UUID)
    @Operation(summary = "Get a meeting by UUID")
    public ResponseEntity<ApiResponse<MeetingResponse>> getMeetingByUuid(
            @Parameter(description = "Meeting UUID", required = true)
            @PathVariable(ApiConstant.UUID) String uuid,
            Locale locale
    ) {
        MeetingResponse response = meetingService.getMeetingByUuid(uuid, locale);
        String message = messageUtil.getMessage("meeting.found", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.MEETING_FOUND, message, response)
        );
    }

    @GetMapping(ApiConstant.MEETINGS)
    @Operation(summary = "Get all meetings for a user with pagination")
    public ResponseEntity<ApiResponse<PageResponse<MeetingResponse>>> getAllMeetingsByUser(
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
        PageResponse<MeetingResponse> response = meetingService.getAllMeetingsByUser(userUuid, pageable, locale);

        String message = messageUtil.getMessage("meetings.found", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.MEETINGS_FOUND, message, response)
        );
    }

    @GetMapping(ApiConstant.MEETINGS + "/date")
    @Operation(summary = "Get all meetings for a user on a specific date")
    public ResponseEntity<ApiResponse<List<MeetingResponse>>> getAllMeetingsByUserAndDate(
            @Parameter(description = "User UUID", required = true)
            @RequestParam String userUuid,
            @Parameter(description = "Meeting date (format: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Locale locale
    ) {
        List<MeetingResponse> response = meetingService.getAllMeetingsByUserAndDate(userUuid, date, locale);
        String message = messageUtil.getMessage("meetings.found", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.MEETINGS_FOUND, message, response)
        );
    }

    @GetMapping(ApiConstant.MEETINGS + "/between")
    @Operation(summary = "Get all meetings for a user between two dates")
    public ResponseEntity<ApiResponse<List<MeetingResponse>>> getAllMeetingsByUserAndDateBetween(
            @Parameter(description = "User UUID", required = true)
            @RequestParam String userUuid,
            @Parameter(description = "Start date (format: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (format: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Locale locale
    ) {
        List<MeetingResponse> response = meetingService.getAllMeetingsByUserAndDateBetween(userUuid, startDate, endDate, locale);
        String message = messageUtil.getMessage("meetings.found", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.MEETINGS_FOUND, message, response)
        );
    }

    @PutMapping(ApiConstant.MEETING_BY_UUID)
    @Operation(summary = "Update a meeting by UUID")
    public ResponseEntity<ApiResponse<MeetingResponse>> updateMeeting(
            @Parameter(description = "Meeting UUID", required = true)
            @PathVariable(ApiConstant.UUID) String uuid,
            @Valid @RequestBody MeetingUpdateRequest request,
            Locale locale
    ) {
        MeetingResponse response = meetingService.updateMeeting(uuid, request, locale);
        String message = messageUtil.getMessage("meeting.updated", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.MEETING_UPDATED, message, response)
        );
    }

    @DeleteMapping(ApiConstant.MEETING_BY_UUID)
    @Operation(summary = "Delete a meeting by UUID")
    public ResponseEntity<ApiResponse<Void>> deleteMeeting(
            @Parameter(description = "Meeting UUID", required = true)
            @PathVariable(ApiConstant.UUID) String uuid,
            Locale locale
    ) {
        meetingService.deleteMeeting(uuid, locale);
        String message = messageUtil.getMessage("meeting.deleted", locale);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.MEETING_DELETED, message)
        );
    }
}
