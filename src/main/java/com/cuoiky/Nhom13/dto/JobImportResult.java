package com.cuoiky.Nhom13.dto;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class JobImportResult {
    int totalRows;
    int imported;
    int failed;
    @Singular
    List<String> errors;
}
