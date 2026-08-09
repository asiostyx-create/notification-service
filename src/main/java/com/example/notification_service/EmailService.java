package com.example.notification_service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private String from;
    private String host;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.mail.from}") String from,
                        @Value("${app.host}") String host) {
        this.mailSender = mailSender;
        this.from = from;
        this.host = host;
    }

    public void sendCreatedNotification(String username, String email) {
        String subject;
        String text;
        subject = "Вы зарегистрировались на сайте";
        text = String.format("Здравствуйте! Ваш аккаунт на сайте %s был успешно создан.", host);
        sendEmail(email, subject, text);
    }
    public void sendDeletedNotification(String username, String email) {
        String subject;
        String text;
        subject = "Удаление аккаунта";
        text = String.format("Здравствуйте! Ваш аккаунт был удален.");
        sendEmail(email, subject, text);
    }

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
        log.info("Письмо успешно отправлено на email: {}", to);
    }
}
