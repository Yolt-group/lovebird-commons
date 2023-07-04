package com.yolt.sample.kafka;

import lombok.ToString;
import lombok.Value;

import java.util.UUID;

/**
 * To avoid accidental logging of PII always explicitly annotate the fields
 * to include in the string representation.
 * <p>
 * Note that {@link lombok.Value} always includes all fields by default.
 */
@ToString(onlyExplicitlyIncluded = true)
@Value
public class MessageDto {

    @ToString.Include
    UUID userId;
    String contents;

}