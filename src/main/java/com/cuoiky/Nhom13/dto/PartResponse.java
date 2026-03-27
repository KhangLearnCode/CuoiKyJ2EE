package com.cuoiky.Nhom13.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class PartResponse {
    Long id;
    String partCode;
    String barcode;
    String partName;
    String unit;
    Integer stockQuantity;
    Integer minimumStockLevel;
    Boolean active;
    List<String> stepTemplates;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
