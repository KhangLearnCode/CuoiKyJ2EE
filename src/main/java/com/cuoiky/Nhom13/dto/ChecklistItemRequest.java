package com.cuoiky.Nhom13.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChecklistItemRequest {
    @NotBlank
    @Size(max = 255)
    private String itemName;

    @Size(max = 500)
    private String note;
}
