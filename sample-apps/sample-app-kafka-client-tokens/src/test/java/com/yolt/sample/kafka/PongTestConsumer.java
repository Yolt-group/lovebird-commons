package com.yolt.sample.kafka;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static nl.ing.lovebird.clienttokens.constants.ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME;

@Component
@Slf4j
public class PongTestConsumer {

    private final Map<UUID, List<Event>> eventsByUserId = new HashMap<>();

    @KafkaListener(topics = "pong", concurrency = "1")
    public void consume(
            @Header(value = CLIENT_TOKEN_HEADER_NAME) final ClientUserToken clientUserToken,
            @Payload MessageDto messageDto) {
        List<Event> events = eventsByUserId.computeIfAbsent(clientUserToken.getUserIdClaim(), x -> new ArrayList<>());
        events.add(new Event(clientUserToken.getUserIdClaim(), clientUserToken, messageDto));
    }

    public List<Event> getAllForUserId(final UUID userId) {
        return eventsByUserId.getOrDefault(userId, new ArrayList<>());
    }

    public void removeAllForUser(final UUID userId) {
        eventsByUserId.remove(userId);
    }

    @Getter
    @RequiredArgsConstructor
    static class Event {
        final UUID userId;
        final ClientUserToken clientUserToken;
        final MessageDto payload;
    }
}

