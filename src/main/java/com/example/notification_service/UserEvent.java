package com.example.notification_service;

public record UserEvent(
        String email,
        String username
) {}
