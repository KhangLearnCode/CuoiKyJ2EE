package com.cuoiky.Nhom13.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PartStockAdjustRequest {
    @NotNull
    private Integer deltaQuantity;
}
