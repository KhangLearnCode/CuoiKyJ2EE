package com.cuoiky.Nhom13.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JobPartUsageRequest {
    @NotNull
    private Long partId;

    @NotNull
    @Min(1)
    private Integer quantityUsed;

    @Size(max = 500)
    private String note;
}
