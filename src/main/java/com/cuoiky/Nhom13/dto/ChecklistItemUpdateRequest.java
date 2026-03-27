package com.cuoiky.Nhom13.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChecklistItemUpdateRequest {
    @NotNull
    private Boolean completed;

    @Size(max = 500)
    private String note;
}
