package com.cuoiky.Nhom13.dto;

import com.cuoiky.Nhom13.model.JobPriority;
import com.cuoiky.Nhom13.model.JobStatus;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class JobStatsResponse {
    long total;
    @Singular("statusCount")
    Map<JobStatus, Long> byStatus;
    @Singular("priorityCount")
    Map<JobPriority, Long> byPriority;
}
