package com.cuoiky.Nhom13.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JobAssignmentRequest {
    @NotNull
    private Long technicianId;

    @Size(max = 500)
    private String note;
}
