package com.cuoiky.Nhom13.service;

import com.cuoiky.Nhom13.dto.ChecklistItemRequest;
import com.cuoiky.Nhom13.dto.ChecklistItemResponse;
import com.cuoiky.Nhom13.dto.ChecklistItemUpdateRequest;
import com.cuoiky.Nhom13.dto.JobAssignmentRequest;
import com.cuoiky.Nhom13.dto.JobActivityResponse;
import com.cuoiky.Nhom13.dto.JobExportResult;
import com.cuoiky.Nhom13.dto.JobImportResult;
import com.cuoiky.Nhom13.dto.JobPartUsageRequest;
import com.cuoiky.Nhom13.dto.JobPartUsageResponse;
import com.cuoiky.Nhom13.dto.JobRequest;
import com.cuoiky.Nhom13.dto.JobImageResponse;
import com.cuoiky.Nhom13.dto.JobResponse;
import com.cuoiky.Nhom13.dto.JobStatsResponse;
import com.cuoiky.Nhom13.dto.PageResponse;
import com.cuoiky.Nhom13.model.Assignment;
import com.cuoiky.Nhom13.model.ERole;
import com.cuoiky.Nhom13.model.Job;
import com.cuoiky.Nhom13.model.JobActivity;
import com.cuoiky.Nhom13.model.JobActivityType;
import com.cuoiky.Nhom13.model.JobChecklistItem;
import com.cuoiky.Nhom13.model.JobImage;
import com.cuoiky.Nhom13.model.JobPartUsage;
import com.cuoiky.Nhom13.model.JobPriority;
import com.cuoiky.Nhom13.model.JobStatus;
import com.cuoiky.Nhom13.model.Part;
import com.cuoiky.Nhom13.model.User;
import com.cuoiky.Nhom13.repository.AssignmentRepository;
import com.cuoiky.Nhom13.repository.JobActivityRepository;
import com.cuoiky.Nhom13.repository.JobChecklistItemRepository;
import com.cuoiky.Nhom13.repository.JobImageRepository;
import com.cuoiky.Nhom13.repository.JobPartUsageRepository;
import com.cuoiky.Nhom13.repository.JobRepository;
import com.cuoiky.Nhom13.repository.PartRepository;
import com.cuoiky.Nhom13.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class JobService {
    private static final DateTimeFormatter CODE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final JobRepository jobRepository;
    private final AssignmentRepository assignmentRepository;
    private final JobActivityRepository jobActivityRepository;
    private final UserRepository userRepository;
    private final JobChecklistItemRepository checklistItemRepository;
    private final PartRepository partRepository;
    private final JobPartUsageRepository jobPartUsageRepository;
    private final JobImageRepository jobImageRepository;
    private final JobStorageService jobStorageService;
    private final JobReportService jobReportService;
    private final NotificationService notificationService;

    public JobService(JobRepository jobRepository,
                      AssignmentRepository assignmentRepository,
                      JobActivityRepository jobActivityRepository,
                      UserRepository userRepository,
                      JobChecklistItemRepository checklistItemRepository,
                      PartRepository partRepository,
                      JobPartUsageRepository jobPartUsageRepository,
                      JobImageRepository jobImageRepository,
                      JobStorageService jobStorageService,
                      JobReportService jobReportService,
                      NotificationService notificationService) {
        this.jobRepository = jobRepository;
        this.assignmentRepository = assignmentRepository;
        this.jobActivityRepository = jobActivityRepository;
        this.userRepository = userRepository;
        this.checklistItemRepository = checklistItemRepository;
        this.partRepository = partRepository;
        this.jobPartUsageRepository = jobPartUsageRepository;
        this.jobImageRepository = jobImageRepository;
        this.jobStorageService = jobStorageService;
        this.jobReportService = jobReportService;
        this.notificationService = notificationService;
    }

    public JobResponse create(JobRequest request) {
        Job job = new Job();
        applyJobRequest(job, request);
        job.setJobCode(generateJobCode());
        job.setStatus(JobStatus.CREATED);
        Job savedJob = jobRepository.save(job);
        saveActivity(savedJob, JobActivityType.CREATED, null, null, JobStatus.CREATED,
                "Work order created");
        notificationService.notifyJobCreated(savedJob);
        return toResponse(jobRepository.findById(savedJob.getId()).orElseThrow());
    }

    public JobResponse update(Long id, JobRequest request, String username) {
        Job job = getJobEntity(id);
        applyJobRequest(job, request);
        Job savedJob = jobRepository.save(job);
        User actor = username != null ? userRepository.findByUsername(username).orElse(null) : null;
        saveActivity(savedJob, JobActivityType.UPDATED, actor, savedJob.getStatus(), savedJob.getStatus(),
                "Work order details updated");
        return toResponse(jobRepository.findById(savedJob.getId()).orElseThrow());
    }

    @Transactional(readOnly = true)
    public PageResponse<JobResponse> search(String keyword, JobStatus status, JobPriority priority, Long assignedUserId,
                                            int page, int size, String sortBy, String sortDir) {
        Specification<Job> specification = buildSpecification(keyword, status, priority, assignedUserId);
        Sort sort = Sort.by(parseSortDirection(sortDir), sanitizeSortBy(sortBy));
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), sort);
        Page<Job> result = jobRepository.findAll(specification, pageable);
        return PageResponse.<JobResponse>builder()
                .content(result.getContent().stream().map(this::toResponse).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .first(result.isFirst())
                .last(result.isLast())
                .sortBy(sanitizeSortBy(sortBy))
                .sortDirection(parseSortDirection(sortDir).name())
                .build();
    }

    @Transactional(readOnly = true)
    public JobResponse getById(Long id) {
        return toResponse(getJobEntity(id));
    }

    @Transactional(readOnly = true)
    public JobExportResult exportJobs(String format,
                                      String keyword,
                                      JobStatus status,
                                      JobPriority priority,
                                      Long assignedUserId,
                                      String sortBy,
                                      String sortDir) {
        List<Job> jobs = jobRepository.findAll(
                buildSpecification(keyword, status, priority, assignedUserId),
                Sort.by(parseSortDirection(sortDir), sanitizeSortBy(sortBy)));
        if ("xlsx".equalsIgnoreCase(format)) {
            return exportXlsx(jobs);
        }
        return exportCsv(jobs);
    }

    public JobImportResult importJobsFromCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("CSV file is required");
        }
        int imported = 0;
        int totalRows = 0;
        List<String> errors = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1 && line.toLowerCase().contains("title") && line.toLowerCase().contains("customer")) {
                    continue; // header
                }
                if (!StringUtils.hasText(line)) {
                    continue;
                }
                totalRows++;
                String[] columns = line.split(",", -1);
                try {
                    if (columns.length < 5) {
                        throw new IllegalArgumentException("Expected at least 5 columns: title, customerName, serviceAddress, scheduledDate(yyyy-MM-dd), priority[,description]");
                    }
                    JobRequest request = new JobRequest();
                    request.setTitle(columns[0].trim());
                    request.setCustomerName(columns[1].trim());
                    request.setServiceAddress(columns[2].trim());
                    request.setScheduledDate(parseDate(columns[3]));
                    request.setPriority(parsePriority(columns[4]));
                    if (columns.length >= 6) {
                        request.setDescription(columns[5].trim());
                    }
                    create(request);
                    imported++;
                } catch (Exception ex) {
                    errors.add("Row " + lineNumber + ": " + ex.getMessage());
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read CSV file", ex);
        }
        int failed = Math.max(0, totalRows - imported);
        return JobImportResult.builder()
                .totalRows(totalRows)
                .imported(imported)
                .failed(failed)
                .errors(errors)
                .build();
    }

    @Transactional(readOnly = true)
    public JobStatsResponse stats(Long assignedUserId) {
        List<Job> jobs = jobRepository.findAll(buildSpecification(null, null, null, assignedUserId));
        Map<JobStatus, Long> byStatus = new EnumMap<>(JobStatus.class);
        Map<JobPriority, Long> byPriority = new EnumMap<>(JobPriority.class);
        for (JobStatus value : JobStatus.values()) {
            byStatus.put(value, 0L);
        }
        for (JobPriority value : JobPriority.values()) {
            byPriority.put(value, 0L);
        }
        for (Job job : jobs) {
            byStatus.computeIfPresent(job.getStatus(), (k, v) -> v + 1);
            byPriority.computeIfPresent(job.getPriority(), (k, v) -> v + 1);
        }
        return JobStatsResponse.builder()
                .total(jobs.size())
                .byStatus(byStatus)
                .byPriority(byPriority)
                .build();
    }

    public JobResponse assign(Long jobId, JobAssignmentRequest request, String assignedByUsername) {
        Job job = getJobEntity(jobId);
        JobStatus previousStatus = job.getStatus();
        if (previousStatus == JobStatus.COMPLETED || previousStatus == JobStatus.CANCELLED) {
            throw new IllegalArgumentException("Completed or cancelled jobs cannot be reassigned");
        }
        User technician = userRepository.findById(request.getTechnicianId())
                .orElseThrow(() -> new IllegalArgumentException("Technician not found"));
        User assignedBy = userRepository.findByUsername(assignedByUsername)
                .orElseThrow(() -> new IllegalArgumentException("Assigned by user not found"));

        boolean canReceiveJobs = technician.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_USER || role.getName() == ERole.ROLE_ADMIN);
        if (!canReceiveJobs) {
            throw new IllegalArgumentException("Selected user cannot be assigned to a job");
        }

        job.setAssignedUser(technician);
        if (job.getStatus() == JobStatus.CREATED) {
            job.setStatus(JobStatus.ASSIGNED);
        }

        Assignment assignment = new Assignment();
        assignment.setJob(job);
        assignment.setTechnician(technician);
        assignment.setAssignedBy(assignedBy);
        assignment.setNote(request.getNote());
        assignmentRepository.save(assignment);

        Job savedJob = jobRepository.save(job);
        saveActivity(savedJob, JobActivityType.ASSIGNED, assignedBy, previousStatus, savedJob.getStatus(),
                "Assigned to " + technician.getUsername());
        return toResponse(jobRepository.findById(savedJob.getId()).orElseThrow());
    }

    public JobResponse updateStatus(Long jobId, JobStatus status, String username, boolean isAdmin) {
        Job job = getJobEntity(jobId);
        if (!isAdmin) {
            validateOperator(job, username);
        }
        validateStatusTransition(job.getStatus(), status, job.getAssignedUser() != null);
        User actor = userRepository.findByUsername(username).orElse(null);
        JobStatus previousStatus = job.getStatus();
        job.setStatus(status);
        Job savedJob = jobRepository.save(job);
        saveActivity(savedJob, JobActivityType.STATUS_CHANGED, actor, previousStatus, status,
                "Status changed from " + previousStatus + " to " + status);
        if (status != previousStatus) {
            notificationService.notifyJobStatusChanged(savedJob);
        }
        return toResponse(jobRepository.findById(savedJob.getId()).orElseThrow());
    }

    public JobResponse addChecklistItem(Long jobId, ChecklistItemRequest request, String username, boolean isAdmin) {
        if (!isAdmin) {
            throw new AccessDeniedException("Only admin can add checklist items");
        }
        Job job = getJobEntity(jobId);
        JobChecklistItem item = new JobChecklistItem();
        item.setJob(job);
        item.setItemName(request.getItemName().trim());
        item.setNote(request.getNote());
        checklistItemRepository.save(item);

        User actor = userRepository.findByUsername(username).orElse(null);
        saveActivity(job, JobActivityType.CHECKLIST_UPDATED, actor, job.getStatus(), job.getStatus(),
                "Checklist item added: " + item.getItemName());
        return toResponse(jobRepository.findById(jobId).orElseThrow());
    }

    public JobResponse updateChecklistItem(Long jobId,
                                           Long checklistItemId,
                                           ChecklistItemUpdateRequest request,
                                           String username,
                                           boolean isAdmin) {
        Job job = getJobEntity(jobId);
        if (!isAdmin) {
            validateOperator(job, username);
        }

        JobChecklistItem item = checklistItemRepository.findById(checklistItemId)
                .orElseThrow(() -> new IllegalArgumentException("Checklist item not found"));
        if (!item.getJob().getId().equals(job.getId())) {
            throw new IllegalArgumentException("Checklist item does not belong to this job");
        }

        User actor = userRepository.findByUsername(username).orElse(null);
        item.setCompleted(request.getCompleted());
        item.setNote(request.getNote());
        if (Boolean.TRUE.equals(request.getCompleted())) {
            item.setCompletedAt(LocalDateTime.now());
            item.setCompletedBy(actor);
        } else {
            item.setCompletedAt(null);
            item.setCompletedBy(null);
        }
        checklistItemRepository.save(item);

        saveActivity(job, JobActivityType.CHECKLIST_UPDATED, actor, job.getStatus(), job.getStatus(),
                "Checklist updated: " + item.getItemName() + " = " + item.getCompleted());
        return toResponse(jobRepository.findById(jobId).orElseThrow());
    }

    public JobResponse usePart(Long jobId, JobPartUsageRequest request, String username, boolean isAdmin) {
        Job job = getJobEntity(jobId);
        if (!isAdmin) {
            validateOperator(job, username);
        }
        if (job.getStatus() == JobStatus.COMPLETED || job.getStatus() == JobStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot use parts for completed/cancelled jobs");
        }

        Part part = partRepository.findById(request.getPartId())
                .orElseThrow(() -> new IllegalArgumentException("Part not found"));
        if (!Boolean.TRUE.equals(part.getActive())) {
            throw new IllegalArgumentException("Part is inactive");
        }

        int remaining = part.getStockQuantity() - request.getQuantityUsed();
        if (remaining < 0) {
            throw new IllegalArgumentException("Insufficient stock for part " + part.getPartCode());
        }
        part.setStockQuantity(remaining);
        partRepository.save(part);

        User actor = userRepository.findByUsername(username).orElse(null);
        JobPartUsage usage = new JobPartUsage();
        usage.setJob(job);
        usage.setPart(part);
        usage.setQuantityUsed(request.getQuantityUsed());
        usage.setNote(request.getNote());
        usage.setUsedBy(actor);
        jobPartUsageRepository.save(usage);

        saveActivity(job, JobActivityType.PART_USED, actor, job.getStatus(), job.getStatus(),
                "Used " + usage.getQuantityUsed() + " " + part.getUnit() + " of " + part.getPartCode());
        return toResponse(jobRepository.findById(jobId).orElseThrow());
    }

    public void delete(Long id) {
        Job job = getJobEntity(id);
        jobRepository.delete(job);
    }

    public JobResponse uploadImages(Long jobId, MultipartFile[] files, String username, boolean isAdmin) {
        Job job = getJobEntity(jobId);
        if (!isAdmin) {
            validateOperator(job, username);
        }
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("Please select at least one image");
        }
        User actor = userRepository.findByUsername(username).orElse(null);
        int uploaded = 0;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            JobStorageService.StoredFile storedFile = jobStorageService.storeImage(jobId, file);
            JobImage image = new JobImage();
            image.setJob(job);
            image.setFileName(storedFile.originalFileName());
            image.setContentType(storedFile.contentType());
            image.setStoragePath(storedFile.storagePath());
            image.setUploadedBy(username);
            jobImageRepository.save(image);
            uploaded++;
        }
        if (uploaded == 0) {
            throw new IllegalArgumentException("Please select at least one image");
        }
        saveActivity(job, JobActivityType.IMAGE_UPLOADED, actor, job.getStatus(), job.getStatus(),
                "Uploaded " + uploaded + " image(s)");
        return toResponse(jobRepository.findById(jobId).orElseThrow());
    }

    public JobResponse deleteImage(Long jobId, Long imageId, String username, boolean isAdmin) {
        Job job = getJobEntity(jobId);
        if (!isAdmin) {
            validateOperator(job, username);
        }
        JobImage image = jobImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));
        if (!image.getJob().getId().equals(job.getId())) {
            throw new IllegalArgumentException("Image does not belong to this job");
        }

        jobStorageService.deleteFile(image.getStoragePath());
        job.getImages().removeIf(existing -> existing.getId().equals(imageId));
        jobImageRepository.delete(image);

        User actor = userRepository.findByUsername(username).orElse(null);
        saveActivity(job, JobActivityType.IMAGE_DELETED, actor, job.getStatus(), job.getStatus(),
                "Deleted image " + image.getFileName());
        return toResponse(jobRepository.findById(jobId).orElseThrow());
    }

    public JobResponse saveSignature(Long jobId, String imageData, String username, boolean isAdmin) {
        Job job = getJobEntity(jobId);
        if (!isAdmin) {
            validateOperator(job, username);
        }
        User actor = userRepository.findByUsername(username).orElse(null);
        JobStorageService.StoredFile storedFile = jobStorageService.storeSignature(jobId, imageData);
        job.setSignaturePath(storedFile.storagePath());
        job.setSignatureContentType(storedFile.contentType());
        job.setSignatureSignedBy(username);
        job.setSignatureSignedAt(LocalDateTime.now());
        jobRepository.save(job);
        saveActivity(job, JobActivityType.SIGNATURE_CAPTURED, actor, job.getStatus(), job.getStatus(),
                "Electronic signature captured");
        return toResponse(jobRepository.findById(jobId).orElseThrow());
    }

    public JobResponse saveSignature(Long jobId, MultipartFile signatureFile, String username, boolean isAdmin) {
        if (signatureFile == null || signatureFile.isEmpty()) {
            throw new IllegalArgumentException("Signature file is required");
        }
        Job job = getJobEntity(jobId);
        if (!isAdmin) {
            validateOperator(job, username);
        }
        User actor = userRepository.findByUsername(username).orElse(null);
        JobStorageService.StoredFile storedFile = jobStorageService.storeSignature(jobId, signatureFile);
        job.setSignaturePath(storedFile.storagePath());
        job.setSignatureContentType(storedFile.contentType());
        job.setSignatureSignedBy(username);
        job.setSignatureSignedAt(LocalDateTime.now());
        jobRepository.save(job);
        saveActivity(job, JobActivityType.SIGNATURE_CAPTURED, actor, job.getStatus(), job.getStatus(),
                "Electronic signature captured");
        return toResponse(jobRepository.findById(jobId).orElseThrow());
    }

    public JobResponse deleteSignature(Long jobId, String username, boolean isAdmin) {
        Job job = getJobEntity(jobId);
        if (!isAdmin) {
            validateOperator(job, username);
        }
        if (!StringUtils.hasText(job.getSignaturePath())) {
            throw new IllegalArgumentException("Signature not found");
        }

        jobStorageService.deleteSignature(job.getSignaturePath());
        job.setSignaturePath(null);
        job.setSignatureContentType(null);
        job.setSignatureSignedBy(null);
        job.setSignatureSignedAt(null);
        jobRepository.save(job);

        User actor = userRepository.findByUsername(username).orElse(null);
        saveActivity(job, JobActivityType.SIGNATURE_CAPTURED, actor, job.getStatus(), job.getStatus(),
                "Electronic signature deleted");
        return toResponse(jobRepository.findById(jobId).orElseThrow());
    }

    @Transactional(readOnly = true)
    public JobImage getJobImage(Long jobId, Long imageId, String username, boolean isAdmin) {
        Job job = getJobEntity(jobId);
        if (!isAdmin) {
            validateOperator(job, username);
        }
        JobImage image = jobImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));
        if (!image.getJob().getId().equals(job.getId())) {
            throw new IllegalArgumentException("Image does not belong to this job");
        }
        return image;
    }

    @Transactional(readOnly = true)
    public Job getJobForSignature(Long jobId, String username, boolean isAdmin) {
        Job job = getJobEntity(jobId);
        if (!isAdmin) {
            validateOperator(job, username);
        }
        if (!StringUtils.hasText(job.getSignaturePath())) {
            throw new IllegalArgumentException("Signature not found");
        }
        return job;
    }

    public byte[] exportReport(Long jobId, String username, boolean isAdmin) {
        Job job = getJobEntity(jobId);
        if (!isAdmin) {
            validateOperator(job, username);
        }
        User actor = userRepository.findByUsername(username).orElse(null);
        byte[] report = jobReportService.generate(job);
        saveActivity(job, JobActivityType.REPORT_EXPORTED, actor, job.getStatus(), job.getStatus(),
                "PDF report exported");
        return report;
    }

    private JobExportResult exportCsv(List<Job> jobs) {
        StringBuilder builder = new StringBuilder();
        builder.append("Job Code,Title,Customer,Address,Scheduled Date,Status,Priority,Assigned User,Created At\n");
        for (Job job : jobs) {
            builder.append(safeCsv(job.getJobCode())).append(',')
                    .append(safeCsv(job.getTitle())).append(',')
                    .append(safeCsv(job.getCustomerName())).append(',')
                    .append(safeCsv(job.getServiceAddress())).append(',')
                    .append(safeCsv(job.getScheduledDate() != null ? job.getScheduledDate().toString() : ""))
                    .append(',')
                    .append(safeCsv(job.getStatus().name())).append(',')
                    .append(safeCsv(job.getPriority().name())).append(',')
                    .append(safeCsv(job.getAssignedUser() != null ? job.getAssignedUser().getUsername() : ""))
                    .append(',')
                    .append(safeCsv(job.getCreatedAt() != null ? job.getCreatedAt().toString() : ""))
                    .append('\n');
        }
        return JobExportResult.builder()
                .content(builder.toString().getBytes(StandardCharsets.UTF_8))
                .filename("jobs-" + System.currentTimeMillis() + ".csv")
                .contentType("text/csv")
                .build();
    }

    private JobExportResult exportXlsx(List<Job> jobs) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Jobs");
            String[] headers = new String[]{"Job Code", "Title", "Customer", "Address", "Scheduled Date", "Status", "Priority", "Assigned User", "Created At"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            int rowIndex = 1;
            for (Job job : jobs) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(job.getJobCode());
                row.createCell(1).setCellValue(defaultString(job.getTitle()));
                row.createCell(2).setCellValue(defaultString(job.getCustomerName()));
                row.createCell(3).setCellValue(defaultString(job.getServiceAddress()));
                row.createCell(4).setCellValue(job.getScheduledDate() != null ? job.getScheduledDate().toString() : "");
                row.createCell(5).setCellValue(job.getStatus().name());
                row.createCell(6).setCellValue(job.getPriority().name());
                row.createCell(7).setCellValue(job.getAssignedUser() != null ? job.getAssignedUser().getUsername() : "");
                row.createCell(8).setCellValue(job.getCreatedAt() != null ? job.getCreatedAt().toString() : "");
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(outputStream);
            return JobExportResult.builder()
                    .content(outputStream.toByteArray())
                    .filename("jobs-" + System.currentTimeMillis() + ".xlsx")
                    .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .build();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to export jobs", ex);
        }
    }

    private Specification<Job> buildSpecification(String keyword, JobStatus status, JobPriority priority, Long assignedUserId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("jobCode")), pattern),
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("customerName")), pattern),
                        cb.like(cb.lower(root.get("serviceAddress")), pattern)
                ));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }

            if (assignedUserId != null) {
                predicates.add(cb.equal(root.get("assignedUser").get("id"), assignedUserId));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private LocalDate parseDate(String value) {
        if (!StringUtils.hasText(value)) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid scheduledDate: " + value);
        }
    }

    private JobPriority parsePriority(String value) {
        if (!StringUtils.hasText(value)) {
            return JobPriority.MEDIUM;
        }
        try {
            return JobPriority.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid priority: " + value);
        }
    }

    private String safeCsv(String value) {
        if (value == null) {
            return "\"\"";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private String defaultString(String value) {
        return value != null ? value : "";
    }

    private void applyJobRequest(Job job, JobRequest request) {
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setCustomerName(request.getCustomerName());
        job.setServiceAddress(request.getServiceAddress());
        job.setScheduledDate(request.getScheduledDate());
        job.setPriority(request.getPriority());
    }

    private Job getJobEntity(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with id " + id));
    }

    private String generateJobCode() {
        return "JOB-" + LocalDateTime.now().format(CODE_FORMATTER);
    }

    private void validateOperator(Job job, String username) {
        if (job.getAssignedUser() == null || !job.getAssignedUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You can only operate on your assigned jobs");
        }
    }

    private void validateStatusTransition(JobStatus currentStatus, JobStatus newStatus, boolean hasAssignee) {
        if (currentStatus == newStatus) {
            return;
        }
        switch (currentStatus) {
            case CREATED -> {
                if (newStatus != JobStatus.ASSIGNED && newStatus != JobStatus.CANCELLED) {
                    throw new IllegalArgumentException("CREATED jobs can only move to ASSIGNED or CANCELLED");
                }
                if (newStatus == JobStatus.ASSIGNED && !hasAssignee) {
                    throw new IllegalArgumentException("Job must be assigned before moving to ASSIGNED");
                }
            }
            case ASSIGNED -> {
                if (newStatus != JobStatus.IN_PROGRESS && newStatus != JobStatus.CANCELLED) {
                    throw new IllegalArgumentException("ASSIGNED jobs can only move to IN_PROGRESS or CANCELLED");
                }
            }
            case IN_PROGRESS -> {
                if (newStatus != JobStatus.COMPLETED && newStatus != JobStatus.CANCELLED) {
                    throw new IllegalArgumentException("IN_PROGRESS jobs can only move to COMPLETED or CANCELLED");
                }
            }
            case COMPLETED, CANCELLED -> throw new IllegalArgumentException("Completed or cancelled jobs cannot change status");
        }
    }

    private Sort.Direction parseSortDirection(String sortDir) {
        return "ASC".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    private String sanitizeSortBy(String sortBy) {
        List<String> allowed = List.of("createdAt", "scheduledDate", "priority", "status", "title", "jobCode");
        return allowed.contains(sortBy) ? sortBy : "createdAt";
    }

    private void saveActivity(Job job, JobActivityType type, User actor, JobStatus fromStatus, JobStatus toStatus, String message) {
        JobActivity activity = new JobActivity();
        activity.setJob(job);
        activity.setActivityType(type);
        activity.setPerformedBy(actor);
        activity.setFromStatus(fromStatus);
        activity.setToStatus(toStatus);
        activity.setMessage(message);
        jobActivityRepository.save(activity);
    }

    private JobResponse toResponse(Job job) {
        User assignedUser = job.getAssignedUser();
        List<JobActivityResponse> timeline = job.getActivities().stream()
                .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
                .map(activity -> JobActivityResponse.builder()
                        .id(activity.getId())
                        .activityType(activity.getActivityType())
                        .performedBy(activity.getPerformedBy() != null ? activity.getPerformedBy().getUsername() : "system")
                        .fromStatus(activity.getFromStatus())
                        .toStatus(activity.getToStatus())
                        .message(activity.getMessage())
                        .createdAt(activity.getCreatedAt())
                        .build())
                .toList();

        List<ChecklistItemResponse> checklist = job.getChecklistItems().stream()
                .sorted(Comparator.comparing(JobChecklistItem::getCreatedAt))
                .map(item -> ChecklistItemResponse.builder()
                        .id(item.getId())
                        .itemName(item.getItemName())
                        .completed(item.getCompleted())
                        .note(item.getNote())
                        .completedBy(item.getCompletedBy() != null ? item.getCompletedBy().getUsername() : null)
                        .completedAt(item.getCompletedAt())
                        .createdAt(item.getCreatedAt())
                        .updatedAt(item.getUpdatedAt())
                        .build())
                .toList();

        List<JobPartUsageResponse> usedParts = job.getPartUsages().stream()
                .sorted((left, right) -> right.getUsedAt().compareTo(left.getUsedAt()))
                .map(usage -> JobPartUsageResponse.builder()
                        .id(usage.getId())
                        .partId(usage.getPart().getId())
                        .partCode(usage.getPart().getPartCode())
                        .partName(usage.getPart().getPartName())
                        .unit(usage.getPart().getUnit())
                        .quantityUsed(usage.getQuantityUsed())
                        .note(usage.getNote())
                        .usedBy(usage.getUsedBy() != null ? usage.getUsedBy().getUsername() : "system")
                        .usedAt(usage.getUsedAt())
                        .build())
                .toList();

        List<JobImageResponse> images = job.getImages().stream()
                .sorted((left, right) -> right.getUploadedAt().compareTo(left.getUploadedAt()))
                .map(image -> JobImageResponse.builder()
                        .id(image.getId())
                        .fileName(image.getFileName())
                        .contentType(image.getContentType())
                        .uploadedBy(image.getUploadedBy())
                        .uploadedAt(image.getUploadedAt())
                        .imageUrl("/api/jobs/" + job.getId() + "/images/" + image.getId())
                        .build())
                .toList();

        return JobResponse.builder()
                .id(job.getId())
                .jobCode(job.getJobCode())
                .title(job.getTitle())
                .description(job.getDescription())
                .customerName(job.getCustomerName())
                .serviceAddress(job.getServiceAddress())
                .scheduledDate(job.getScheduledDate())
                .status(job.getStatus())
                .priority(job.getPriority())
                .assignedUserId(assignedUser != null ? assignedUser.getId() : null)
                .assignedUsername(assignedUser != null ? assignedUser.getUsername() : null)
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .timeline(timeline)
                .checklist(checklist)
                .usedParts(usedParts)
                .images(images)
                .signatureImageUrl(StringUtils.hasText(job.getSignaturePath()) ? "/api/jobs/" + job.getId() + "/signature/image" : null)
                .signatureSignedBy(job.getSignatureSignedBy())
                .signatureSignedAt(job.getSignatureSignedAt())
                .reportPdfUrl("/api/jobs/" + job.getId() + "/report")
                .build();
    }
}
