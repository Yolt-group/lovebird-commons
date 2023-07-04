package com.yolt.compliance.gdpr.client.messaging.consumer.protocol;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@Builder
@RequiredArgsConstructor
public class GDPRDataRequest {

    @NonNull
    private final UUID userId; // backwards compatibility; should be replaces with KafkaHeader#X-USER-ID

    @NonNull
    private final UUID requestId;
}
