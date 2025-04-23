package com.ptit.projectmanagementbe.dto.request.schedule;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ScheduleUpdateRequest {

    private LocalDate scheduleDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private String taskUuid;

    private String meetingUuid;
}
