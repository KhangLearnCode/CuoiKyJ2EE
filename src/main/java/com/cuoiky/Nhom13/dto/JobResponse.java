package com.cuoiky.Nhom13.dto;

import com.cuoiky.Nhom13.model.JobPriority;
import com.cuoiky.Nhom13.model.JobStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class JobResponse {
    Long id;
    String jobCode;
    String title;
    String description;
    String customerName;
    String serviceAddress;
    LocalDate scheduledDate;
    JobStatus status;
    JobPriority priority;
    Long assignedUserId;
    String assignedUsername;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    List<JobActivityResponse> timeline;
    List<ChecklistItemResponse> checklist;
    List<JobPartUsageResponse> usedParts;
}
