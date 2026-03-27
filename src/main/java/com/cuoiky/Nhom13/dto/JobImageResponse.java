package com.cuoiky.Nhom13.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class JobImageResponse {
    Long id;
    String fileName;
    String contentType;
    String uploadedBy;
    LocalDateTime uploadedAt;
    String imageUrl;
}
