package com.yolt.compliance.gdpr.client;

import com.yolt.compliance.gdpr.client.messaging.producer.GDPRDataReply;

public class TestUtils {

    public static GDPRDataReply.GDPRDataReplyBuilder createDefaultGDPRDataReplyKeyBuilder() {

        final GDPRDataReply.GDPRDataInfo dataInfo = GDPRDataReply.GDPRDataInfo.builder()
                .fileName("file-name")
                .fileFormat("zip")
                .build();

        return GDPRDataReply.builder()
                .userId(TestConstants.USER_ID_1)
                .requestId(TestConstants.REQUEST_ID_1)
                .serviceId(TestConstants.SERVICE_ID_1)
                .dataInfo(dataInfo);
    }
}
