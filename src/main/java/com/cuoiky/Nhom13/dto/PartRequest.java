package com.cuoiky.Nhom13.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PartRequest {
    @NotBlank
    @Size(max = 60)
    private String partCode;

    @Size(max = 120)
    private String barcode;

    @NotBlank
    @Size(max = 150)
    private String partName;

    @NotBlank
    @Size(max = 30)
    private String unit;

    @NotNull
    @Min(0)
    private Integer stockQuantity;

    @NotNull
    @Min(0)
    private Integer minimumStockLevel;

    @NotNull
    private Boolean active;

    private List<@Size(max = 255) String> stepTemplates;
}
