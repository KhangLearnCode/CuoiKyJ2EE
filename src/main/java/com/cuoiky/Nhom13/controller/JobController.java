package com.cuoiky.Nhom13.controller;

import com.cuoiky.Nhom13.dto.JobAssignmentRequest;
import com.cuoiky.Nhom13.dto.JobExportResult;
import com.cuoiky.Nhom13.dto.JobImportResult;
import com.cuoiky.Nhom13.dto.JobRequest;
import com.cuoiky.Nhom13.dto.JobResponse;
import com.cuoiky.Nhom13.dto.JobStatsResponse;
import com.cuoiky.Nhom13.dto.JobStatusUpdateRequest;
import com.cuoiky.Nhom13.dto.PageResponse;
import com.cuoiky.Nhom13.model.JobPriority;
import com.cuoiky.Nhom13.model.JobStatus;
import com.cuoiky.Nhom13.security.UserDetailsImpl;
import com.cuoiky.Nhom13.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PageResponse<JobResponse>> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) JobPriority priority,
            @RequestParam(required = false) Long assignedUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        return ResponseEntity.ok(jobService.search(keyword, status, priority, assignedUserId, page, size, sortBy, sortDir));
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportJobs(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) JobPriority priority,
            @RequestParam(required = false) Long assignedUserId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        JobExportResult result = jobService.exportJobs(format, keyword, status, priority, assignedUserId, sortBy, sortDir);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.getFilename() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, result.getContentType())
                .body(result.getContent());
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobImportResult> importJobs(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(jobService.importJobsFromCsv(file));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<JobStatsResponse> stats(
            @RequestParam(required = false) Long assignedUserId,
            Authentication authentication) {
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        Long effectiveAssignedUserId = isAdmin ? assignedUserId : currentUser.getId();
        return ResponseEntity.ok(jobService.stats(effectiveAssignedUserId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<JobResponse> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody JobRequest request) {
        return ResponseEntity.ok(jobService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobRequest request,
            Authentication authentication) {
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(jobService.update(id, request, currentUser.getUsername()));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobResponse> assignJob(
            @PathVariable Long id,
            @Valid @RequestBody JobAssignmentRequest request,
            Authentication authentication) {
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(jobService.assign(id, request, currentUser.getUsername()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<JobResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody JobStatusUpdateRequest request,
            Authentication authentication) {
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        return ResponseEntity.ok(jobService.updateStatus(id, request.getStatus(), currentUser.getUsername(), isAdmin));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        jobService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
