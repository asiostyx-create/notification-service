package com.example.notification_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventHandler {

    private final Logger log = LoggerFactory.getLogger(UserEventHandler.class);
    private final EmailService emailService;

    public UserEventHandler(EmailService emailService) {
        this.emailService = emailService;
    }
    @KafkaListener(topics = "user-created-events-topic", groupId = "notification")
    public void handleCreated(UserEvent userEvent) {
        log.info("Получено сообщение: {}", userEvent.email());
        try{
            emailService.sendCreatedNotification(userEvent.username(), userEvent.email());
        } catch (Exception e) {
            log.error("Ошибка отправки почты {}: {}", userEvent.email(), e.getMessage());
        }
    }
    @KafkaListener(topics = "user-deleted-events-topic", groupId = "notification")
    public void handleDeleted(UserEvent userEvent) {
        log.info("Получен event: {}", userEvent.email());
        try{
            emailService.sendDeletedNotification(userEvent.username(), userEvent.email());
        } catch (Exception e) {
            log.error("Ошибка отправки почты {}: {}", userEvent.email(), e.getMessage());
        }
    }
}
