package com.cuoiky.Nhom13.controller;

import com.cuoiky.Nhom13.dto.NotificationResponse;
import com.cuoiky.Nhom13.dto.MessageResponse;
import com.cuoiky.Nhom13.security.UserDetailsImpl;
import com.cuoiky.Nhom13.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            Authentication authentication) {
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.getNotifications(currentUser.getUsername(), unreadOnly));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> unreadCount(Authentication authentication) {
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(Map.of("count", notificationService.countUnread(currentUser.getUsername())));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> markAsRead(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        notificationService.markAsRead(id, currentUser.getUsername());
        return ResponseEntity.ok(new MessageResponse("Notification marked as read"));
    }

    @PatchMapping("/read-all")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> markAllAsRead(Authentication authentication) {
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        notificationService.markAllAsRead(currentUser.getUsername());
        return ResponseEntity.ok(new MessageResponse("All notifications marked as read"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteNotification(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        notificationService.deleteNotification(id, currentUser.getUsername());
        return ResponseEntity.ok(new MessageResponse("Notification deleted"));
    }
}
