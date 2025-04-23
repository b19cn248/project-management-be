package com.ptit.projectmanagementbe.dto.response.task;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TaskResponse {

    private String uuid;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String priority;
    private String status;
    private String projectUuid;
    private String projectName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
