package com.cuoiky.Nhom13.dto;

import com.cuoiky.Nhom13.model.NotificationType;
import com.cuoiky.Nhom13.model.JobStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private Long jobId;
    private JobStatus jobStatus;
    private boolean read;
    private LocalDateTime createdAt;
}
