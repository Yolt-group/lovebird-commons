package com.yolt.sample.kafka;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class PongTestConsumer {

    private final Map<UUID, List<Event>> eventsByUserId = new HashMap<>();

    @KafkaListener(topics = "pong", concurrency = "1")
    public void consume(
            @Payload MessageDto messageDto) {
        List<Event> events = eventsByUserId.computeIfAbsent(messageDto.getUserId(), x -> new ArrayList<>());
        events.add(new Event(messageDto.getUserId(), messageDto));
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
        final MessageDto payload;
    }
}

