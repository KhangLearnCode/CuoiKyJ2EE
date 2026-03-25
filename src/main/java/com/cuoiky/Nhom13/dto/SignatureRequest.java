package com.cuoiky.Nhom13.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignatureRequest {
    @NotBlank
    private String imageData;
}
