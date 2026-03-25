package com.cuoiky.Nhom13.controller;

import com.cuoiky.Nhom13.dto.ChecklistItemRequest;
import com.cuoiky.Nhom13.dto.ChecklistItemUpdateRequest;
import com.cuoiky.Nhom13.dto.JobPartUsageRequest;
import com.cuoiky.Nhom13.dto.JobResponse;
import com.cuoiky.Nhom13.security.UserDetailsImpl;
import com.cuoiky.Nhom13.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/jobs")
public class JobExecutionController {
    private final JobService jobService;

    public JobExecutionController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping("/{id}/checklist")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobResponse> addChecklistItem(@PathVariable Long id,
                                                        @Valid @RequestBody ChecklistItemRequest request,
                                                        Authentication authentication) {
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(jobService.addChecklistItem(id, request, currentUser.getUsername(), true));
    }

    @PatchMapping("/{id}/checklist/{checklistItemId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<JobResponse> updateChecklistItem(@PathVariable Long id,
                                                           @PathVariable Long checklistItemId,
                                                           @Valid @RequestBody ChecklistItemUpdateRequest request,
                                                           Authentication authentication) {
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        return ResponseEntity.ok(jobService.updateChecklistItem(id, checklistItemId, request, currentUser.getUsername(), isAdmin));
    }

    @PostMapping("/{id}/parts-usage")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<JobResponse> usePart(@PathVariable Long id,
                                               @Valid @RequestBody JobPartUsageRequest request,
                                               Authentication authentication) {
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        return ResponseEntity.ok(jobService.usePart(id, request, currentUser.getUsername(), isAdmin));
    }
}
