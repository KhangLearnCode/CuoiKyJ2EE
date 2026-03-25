package com.cuoiky.Nhom13.dto;

import com.cuoiky.Nhom13.model.JobStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobStatusUpdateRequest {
    @NotNull
    private JobStatus status;
}
