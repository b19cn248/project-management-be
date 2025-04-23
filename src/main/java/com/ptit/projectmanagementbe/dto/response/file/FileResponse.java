package com.ptit.projectmanagementbe.dto.response.file;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FileResponse {

    private String uuid;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String contentType;
    private String taskUuid;
    private String projectUuid;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
