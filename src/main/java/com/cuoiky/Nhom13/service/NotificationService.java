package com.cuoiky.Nhom13.service;

import com.cuoiky.Nhom13.dto.NotificationResponse;
import com.cuoiky.Nhom13.model.ERole;
import com.cuoiky.Nhom13.model.Job;
import com.cuoiky.Nhom13.model.JobStatus;
import com.cuoiky.Nhom13.model.JobStatusSyncState;
import com.cuoiky.Nhom13.model.Notification;
import com.cuoiky.Nhom13.model.NotificationType;
import com.cuoiky.Nhom13.model.User;
import com.cuoiky.Nhom13.repository.JobRepository;
import com.cuoiky.Nhom13.repository.JobStatusSyncStateRepository;
import com.cuoiky.Nhom13.repository.NotificationRepository;
import com.cuoiky.Nhom13.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final JobStatusSyncStateRepository jobStatusSyncStateRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               JobRepository jobRepository,
                               JobStatusSyncStateRepository jobStatusSyncStateRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.jobStatusSyncStateRepository = jobStatusSyncStateRepository;
    }

    public void notifyJobCreated(Job job) {
        List<User> recipients = userRepository.findDistinctByRoles_NameOrderByUsernameAsc(ERole.ROLE_ADMIN);
        saveNotifications(recipients, NotificationType.JOB_CREATED,
                "Job moi: " + job.getJobCode(),
                job.getTitle() + " - " + job.getCustomerName(),
                job.getId(),
                null);
        recordCurrentStatus(job, true);
    }

    public void notifyJobStatusChanged(Job job) {
        List<User> recipients = userRepository.findDistinctByRoles_NameOrderByUsernameAsc(ERole.ROLE_ADMIN);
        saveNotifications(recipients, NotificationType.JOB_STATUS_CHANGED,
                "Job cap nhat trang thai: " + job.getJobCode(),
                job.getTitle() + " da chuyen sang " + job.getStatus(),
                job.getId(),
                job.getStatus());
        recordCurrentStatus(job, true);
    }

    @Scheduled(fixedDelayString = "${app.notifications.status-sync-ms:15000}")
    public void syncJobStatusNotifications() {
        for (Job job : jobRepository.findAllByOrderByIdAsc()) {
            JobStatusSyncState syncState = jobStatusSyncStateRepository.findById(job.getId()).orElse(null);
            if (syncState == null) {
                recordCurrentStatus(job, false);
                continue;
            }
            if (!syncState.isInitialized()) {
                syncState.setInitialized(true);
                syncState.setLastKnownStatus(job.getStatus());
                jobStatusSyncStateRepository.save(syncState);
                continue;
            }
            if (syncState.getLastKnownStatus() != job.getStatus()) {
                notifyJobStatusChanged(job);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(String username, boolean unreadOnly) {
        List<Notification> notifications = unreadOnly
                ? notificationRepository.findTop20ByRecipient_UsernameAndReadFalseOrderByCreatedAtDesc(username)
                : notificationRepository.findTop20ByRecipient_UsernameOrderByCreatedAtDesc(username);
        return notifications.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public long countUnread(String username) {
        return notificationRepository.countByRecipient_UsernameAndReadFalse(username);
    }

    public void markAsRead(Long id, String username) {
        Notification notification = notificationRepository.findByIdAndRecipient_Username(id, username)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead(String username) {
        List<Notification> notifications = notificationRepository.findTop20ByRecipient_UsernameAndReadFalseOrderByCreatedAtDesc(username);
        notifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    public void deleteNotification(Long id, String username) {
        Notification notification = notificationRepository.findByIdAndRecipient_Username(id, username)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notificationRepository.delete(notification);
    }

    private void saveNotifications(List<User> recipients,
                                   NotificationType type,
                                   String title,
                                   String message,
                                   Long jobId,
                                   JobStatus jobStatus) {
        if (recipients.isEmpty()) {
            return;
        }
        List<Notification> notifications = recipients.stream()
                .filter(recipient -> !notificationRepository.existsByRecipient_IdAndJobIdAndTypeAndJobStatus(recipient.getId(), jobId, type, jobStatus))
                .map(recipient -> {
                    Notification notification = new Notification();
                    notification.setRecipient(recipient);
                    notification.setType(type);
                    notification.setTitle(title);
                    notification.setMessage(message);
                    notification.setJobId(jobId);
                    notification.setJobStatus(jobStatus);
                    notification.setRead(false);
                    return notification;
                })
                .toList();
        if (notifications.isEmpty()) {
            return;
        }
        notificationRepository.saveAll(notifications);
    }

    private void recordCurrentStatus(Job job, boolean initialized) {
        JobStatusSyncState syncState = jobStatusSyncStateRepository.findById(job.getId())
                .orElseGet(JobStatusSyncState::new);
        syncState.setJob(job);
        syncState.setLastKnownStatus(job.getStatus());
        syncState.setInitialized(initialized);
        jobStatusSyncStateRepository.save(syncState);
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .jobId(notification.getJobId())
                .jobStatus(notification.getJobStatus())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
