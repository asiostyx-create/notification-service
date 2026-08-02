package com.example.notification_service;

import jakarta.validation.constraints.NotBlank;

public record Notification (
    @NotBlank(message = "Имя пользователя не может быть пустым")
    String username,
    @NotBlank(message = "Email не может быть пустым")
    String email
) {}
