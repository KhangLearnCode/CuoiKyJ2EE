package com.cuoiky.Nhom13.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class ChecklistItemResponse {
    Long id;
    String itemName;
    Boolean completed;
    String note;
    String completedBy;
    LocalDateTime completedAt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
