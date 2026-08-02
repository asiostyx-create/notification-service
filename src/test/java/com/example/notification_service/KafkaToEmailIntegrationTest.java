package com.example.notification_service;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(
        partitions = 3,
        topics = {"user-created-events-topic", "user-deleted-events-topic"}
)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.properties.spring.json.trusted.packages=*",

        "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
        "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",

        "spring.mail.host=localhost",
        "spring.mail.port=3025",
        "spring.mail.properties.mail.smtp.auth=false",
        "spring.mail.properties.mail.smtp.starttls.enable=false",
        "app.mail.from=pochta@pochta.ru"
})
class KafkaToEmailIntegrationTest {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication());

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @BeforeEach
    void setUp() throws FolderException {
        greenMail.purgeEmailFromAllMailboxes();

        registry.getListenerContainers().forEach(container ->
                ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic())
        );
    }

    @Test
    @DisplayName("Kafka Created Event")
    void createEmailTest() {
        UserEvent event = new UserEvent("test", "test@example.com");

        kafkaTemplate.send("user-created-events-topic", event.username(), event);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
            assertEquals(1, receivedMessages.length);
            MimeMessage message = receivedMessages[0];
            assertEquals("pochta@pochta.ru", message.getFrom()[0].toString());
            assertEquals("test@example.com", message.getAllRecipients()[0].toString());
            assertTrue(message.getContent().toString().contains("test"));
        });
    }

    @Test
    @DisplayName("Kafka Deleted Event")
    void deleteEmailTest() {
        UserEvent event = new UserEvent("test", "test@example.com");

        kafkaTemplate.send("user-deleted-events-topic", event.username(), event);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
            assertEquals(1, receivedMessages.length);

            MimeMessage message = receivedMessages[0];
            assertEquals("test@example.com", message.getAllRecipients()[0].toString());
            assertTrue(message.getContent().toString().contains("test"));
        });
    }

    @Test
    @DisplayName("Poison Pill")
    void poisonPill() {
        kafkaTemplate.send("user-created-events-topic", "key", "{ invalid_json: ");

        await().during(Duration.ofSeconds(2)).atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            assertEquals(0, greenMail.getReceivedMessages().length);
        });
    }
}