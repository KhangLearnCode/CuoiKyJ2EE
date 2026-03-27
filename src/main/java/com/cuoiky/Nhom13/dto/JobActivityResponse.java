package com.cuoiky.Nhom13.dto;

import com.cuoiky.Nhom13.model.JobActivityType;
import com.cuoiky.Nhom13.model.JobStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class JobActivityResponse {
    Long id;
    JobActivityType activityType;
    String performedBy;
    JobStatus fromStatus;
    JobStatus toStatus;
    String message;
    LocalDateTime createdAt;
}
