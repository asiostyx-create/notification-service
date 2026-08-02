package com.example.notification_service;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final EmailService emailService;

    public NotificationController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send-created")
    public ResponseEntity<Void> sendEmailCreated(@Valid @RequestBody Notification notification) {
        emailService.sendCreatedNotification(notification.username(), notification.email());
        return ResponseEntity.ok().build();
    }
    @PostMapping("/send-deleted")
    public ResponseEntity<Void> sendEmailDeleted(@Valid @RequestBody Notification notification) {
        emailService.sendDeletedNotification(notification.username(), notification.email());
        return ResponseEntity.ok().build();
    }
}