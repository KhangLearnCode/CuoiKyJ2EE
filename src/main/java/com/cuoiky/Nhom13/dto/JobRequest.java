package com.cuoiky.Nhom13.dto;

import com.cuoiky.Nhom13.model.JobPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class JobRequest {
    @NotBlank
    @Size(max = 150)
    private String title;

    @Size(max = 2000)
    private String description;

    @NotBlank
    @Size(max = 120)
    private String customerName;

    @NotBlank
    @Size(max = 255)
    private String serviceAddress;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate scheduledDate;

    @NotNull
    private JobPriority priority;
}
