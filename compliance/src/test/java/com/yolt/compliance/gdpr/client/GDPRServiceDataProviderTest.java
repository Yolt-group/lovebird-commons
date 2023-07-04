package com.yolt.compliance.gdpr.client;

import com.yolt.compliance.gdpr.client.spi.GDPRServiceDataProvider;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GDPRServiceDataProviderTest {

    private final GDPRServiceDataProvider provider = new GDPRServiceDataProvider() {
        @Override
        public Optional<FileMetaAndBytes> getDataFileAsBytes(UUID userId) {
            return Optional.empty();
        }

        @Override
        public String getServiceId() {
            return "dummy";
        }
    };

    @Test
    void testHash() {
        final String hashed = provider.hash("test string");
        assertThat(hashed).isEqualTo("D5579C46DFCC7F18207013E65B44E4CB4E2C2298F4AC457BA8F82743F31E930B");
    }

    @Test
    void testEmptyInput() {
        final String hashed = provider.hash("");
        assertThat(hashed).isEqualTo("");
    }

    @Test
    void testNullInput() {
        final String hashed = provider.hash(null);
        assertThat(hashed).isEqualTo("");
    }
}
