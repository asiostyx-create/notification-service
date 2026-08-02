package com.example.notification_service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendCreatedNotification(String username, String email) {
        String subject;
        String text;
        subject = "Вы зарегистрировались на сайте";
        text = String.format("Здравствуйте, %s! Ваш аккаунт на сайте \"сайт\" был успешно создан.", username);
        sendEmail(email, subject, text);
    }
    public void sendDeletedNotification(String username, String email) {
        String subject;
        String text;
        subject = "Удаление аккаунта";
        text = String.format("Здравствуйте, %s! Ваш аккаунт на сайте \"сайт\" был удален.", username);
        sendEmail(email, subject, text);
    }

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("pochta@pochta.ru");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
        log.info("Письмо успешно отправлено на email: {}", to);
    }
}
