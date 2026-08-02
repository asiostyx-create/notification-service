package com.example.notification_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=3025",
        "spring.mail.properties.mail.smtp.auth=false",
        "spring.mail.properties.mail.smtp.starttls.enable=false",
        "app.mail.from=noreply@example.com"
})
class RestToEmailIntegrationTest {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication());

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws FolderException {
        greenMail.purgeEmailFromAllMailboxes();
    }

    @Test
    @DisplayName("Отправка письма о создании")
    void shouldSendCreatedEmailViaRestEndpoint() throws Exception {
        Notification notification = new Notification("test", "test@example.com");

        mockMvc.perform(post("/notifications/send-created")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isOk());

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(1, receivedMessages.length);
        assertEquals("test@example.com", receivedMessages[0].getAllRecipients()[0].toString());
        assertTrue(receivedMessages[0].getContent().toString().contains("test"));
    }

    @Test
    @DisplayName("Отправка письма при удалении")
    void shouldSendDeletedEmailViaRestEndpoint() throws Exception {
        Notification request = new Notification("test", "test@example.com");

        mockMvc.perform(post("/notifications/send-deleted")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(1, receivedMessages.length);
        assertEquals("test@example.com", receivedMessages[0].getAllRecipients()[0].toString());
    }

    @Test
    @DisplayName("Отправка письма о создании с неверным email")
    void shouldFailValidationAndNotSendEmail() throws Exception {
        Notification invalidRequest = new Notification("", "not-an-email");

        mockMvc.perform(post("/notifications/send-created")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        assertEquals(0, greenMail.getReceivedMessages().length);
    }
}