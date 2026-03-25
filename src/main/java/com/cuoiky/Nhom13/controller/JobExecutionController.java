package com.cuoiky.Nhom13.controller;

import com.cuoiky.Nhom13.dto.ChecklistItemRequest;
import com.cuoiky.Nhom13.dto.ChecklistItemUpdateRequest;
import com.cuoiky.Nhom13.dto.JobPartUsageRequest;
import com.cuoiky.Nhom13.dto.JobResponse;
import com.cuoiky.Nhom13.dto.SignatureRequest;
import com.cuoiky.Nhom13.model.Job;
import com.cuoiky.Nhom13.model.JobImage;
import com.cuoiky.Nhom13.security.UserDetailsImpl;
import com.cuoiky.Nhom13.service.JobService;
import com.cuoiky.Nhom13.service.JobStorageService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/jobs")
public class JobExecutionController {
    private final JobService jobService;
    private final JobStorageService jobStorageService;

    public JobExecutionController(JobService jobService, JobStorageService jobStorageService) {
        this.jobService = jobService;
        this.jobStorageService = jobStorageService;
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

    @PostMapping(path = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<JobResponse> uploadImages(@PathVariable Long id,
                                                    @RequestParam("files") MultipartFile[] files,
                                                    Authentication authentication) {
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        return ResponseEntity.ok(jobService.uploadImages(id, files, currentUser.getUsername(), isAdmin));
    }

    @GetMapping("/{id}/images/{imageId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Resource> getImage(@PathVariable Long id,
                                             @PathVariable Long imageId,
                                             Authentication authentication) {
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        JobImage image = jobService.getJobImage(id, imageId, currentUser.getUsername(), isAdmin);
        Resource resource = jobStorageService.loadAsResource(image.getStoragePath());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(image.getFileName()).build().toString())
                .body(resource);
    }

    @PostMapping("/{id}/signature")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<JobResponse> saveSignature(@PathVariable Long id,
                                                     @Valid @RequestBody SignatureRequest request,
                                                     Authentication authentication) {
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        return ResponseEntity.ok(jobService.saveSignature(id, request.getImageData(), currentUser.getUsername(), isAdmin));
    }

    @GetMapping("/{id}/signature/image")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Resource> getSignatureImage(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        Job job = jobService.getJobForSignature(id, currentUser.getUsername(), isAdmin);
        Resource resource = jobStorageService.loadAsResource(job.getSignaturePath());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(job.getSignatureContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename("signature-" + job.getId() + ".png").build().toString())
                .body(resource);
    }

    @GetMapping("/{id}/report")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportReport(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        byte[] report = jobService.exportReport(id, currentUser.getUsername(), isAdmin);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("job-" + id + "-report.pdf").build().toString())
                .body(report);
    }
}
