package com.ufb.auth.user_management.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufb.auth.user_management.model.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(UserEventPublisher.class);
    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${ufb.kafka.topic.user-registered}")
    private String userRegisteredTopic;

    @Value("${ufb.kafka.topic.user-updated}")
    private String userUpdatedTopic;

    @Value("${ufb.kafka.topic.user-deleted}")
    private String userDeletedTopic;

    public void publishRegistered(User user) {
        publish(userRegisteredTopic, "user.registered", user);
    }

    public void publishUpdated(User user) {
        publish(userUpdatedTopic, "user.updated", user);
    }

    public void publishDeleted(User user) {
        publish(userDeletedTopic, "user.deleted", user);
    }

    private void publish(String topic, String eventType, User user) {
        UserEvent event = new UserEvent(
                eventType,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.isEnabled(),
                Instant.now()
        );
        try {
            String payload = MAPPER.writeValueAsString(event);
            kafkaTemplate.send(topic, user.getEmail(), payload);
            log.info("Published {} for userId={}", eventType, user.getId());
        } catch (JsonProcessingException ex) {
            log.error("Failed to serialize {} for userId={}: {}", eventType, user.getId(), ex.getMessage());
        } catch (Exception ex) {
            log.error("Failed to publish {} for userId={}: {}", eventType, user.getId(), ex.getMessage());
        }
    }
}
