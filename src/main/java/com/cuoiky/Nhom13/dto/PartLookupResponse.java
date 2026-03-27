package com.cuoiky.Nhom13.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class PartLookupResponse {
    Long id;
    String partCode;
    String barcode;
    String partName;
    String unit;
    Integer stockQuantity;
    Integer minimumStockLevel;
    Boolean active;
    List<String> stepTemplates;
}
