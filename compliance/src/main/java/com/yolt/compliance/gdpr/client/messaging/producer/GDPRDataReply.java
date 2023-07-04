package com.yolt.compliance.gdpr.client.messaging.producer;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@Builder
@RequiredArgsConstructor
public class GDPRDataReply {

    @NonNull
    private final UUID userId;

    @NonNull
    private final UUID requestId;

    @NonNull
    private final String serviceId;

    @NonNull
    private final GDPRDataInfo dataInfo;

    @Data
    @Builder
    @RequiredArgsConstructor
    public static class GDPRDataInfo {

        @NonNull
        private final String fileName;

        @NonNull
        private final String fileFormat;
    }
}
