package com.yolt.compliance.gdpr.client.spi;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;

public interface GDPRServiceDataProvider {

    /**
     * @param userId the id of the user for which GDPR is requested.
     * @return Optional filedata, which should never be empty if your gdpr request should succeed (backward compatibility reasons).
     */
    Optional<FileMetaAndBytes> getDataFileAsBytes(UUID userId);

    String getServiceId();

    /**
     * Hash a {@link String} with the `SHA-256` hashing algorithm
     *
     * @param s the {@link String} to hash
     * @return the hex-encoded hashed
     */
    default String hash(final String s) {
        if (s == null || s.length() == 0) {
            return "";
        }

        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new HashingAlgorithmNotAvailable("SHA-256", e);
        }
        return String.format("%064X", new BigInteger(1, digest.digest(s.getBytes(StandardCharsets.UTF_8))));

    }

    class HashingAlgorithmNotAvailable extends RuntimeException {
        HashingAlgorithmNotAvailable(final String algorithm, final Throwable cause) {
            super(String.format("The requested hashing algorithm %s is not available", algorithm), cause);
        }
    }

    @Data
    @Builder
    @RequiredArgsConstructor
    class FileMetadata {

        @NonNull
        private final String name;

        @NonNull
        private final String format;
    }

    @Value
    class FileMetaAndBytes {
        private FileMetadata metadata;
        private byte[] bytes;
    }

}
