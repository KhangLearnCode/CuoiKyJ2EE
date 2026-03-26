package com.cuoiky.Nhom13.repository;

import com.cuoiky.Nhom13.model.Notification;
import com.cuoiky.Nhom13.model.JobStatus;
import com.cuoiky.Nhom13.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop20ByRecipient_UsernameOrderByCreatedAtDesc(String username);

    List<Notification> findTop20ByRecipient_UsernameAndReadFalseOrderByCreatedAtDesc(String username);

    long countByRecipient_UsernameAndReadFalse(String username);

    Optional<Notification> findByIdAndRecipient_Username(Long id, String username);

    boolean existsByRecipient_IdAndJobIdAndTypeAndJobStatus(Long recipientId, Long jobId, NotificationType type, JobStatus jobStatus);

    boolean existsByRecipient_IdAndTypeAndPartId(Long recipientId, NotificationType type, Long partId);

    boolean existsByRecipient_IdAndJobIdAndType(Long recipientId, Long jobId, NotificationType type);
}
