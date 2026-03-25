package com.cuoiky.Nhom13.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class JobPartUsageResponse {
    Long id;
    Long partId;
    String partCode;
    String partName;
    String unit;
    Integer quantityUsed;
    String note;
    String usedBy;
    LocalDateTime usedAt;
}
