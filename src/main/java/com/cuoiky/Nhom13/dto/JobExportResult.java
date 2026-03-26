package com.cuoiky.Nhom13.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class JobExportResult {
    byte[] content;
    String filename;
    String contentType;
}
