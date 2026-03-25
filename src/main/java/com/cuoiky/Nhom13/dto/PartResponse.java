package com.cuoiky.Nhom13.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class PartResponse {
    Long id;
    String partCode;
    String partName;
    String unit;
    Integer stockQuantity;
    Integer minimumStockLevel;
    Boolean active;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
