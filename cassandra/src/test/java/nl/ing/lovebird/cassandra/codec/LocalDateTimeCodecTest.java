package nl.ing.lovebird.cassandra.codec;

import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.exceptions.InvalidTypeException;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class LocalDateTimeCodecTest {
    private static final LocalDateTime DATE_TIME = ZonedDateTime.of(2018, 8, 13, 10, 38, 56, 00, ZoneId.of("UTC"))
            .toLocalDateTime();
    private final LocalDateTimeCodec codec = new LocalDateTimeCodec();

    @Test
    void serialize() {
        ByteBuffer formatted = codec.serialize(DATE_TIME, ProtocolVersion.NEWEST_SUPPORTED);
        assertThat(formatted.getLong()).isEqualTo(1534156736000L);
    }

    @Test
    void serializeFormat() {
        final String formatted = codec.format(DATE_TIME);
        assertThat(formatted).isEqualToIgnoringCase("1534156736000");
    }


    @Test
    void deserialize() {
        ByteBuffer serialized = codec.serialize(DATE_TIME, ProtocolVersion.NEWEST_SUPPORTED);
        LocalDateTime formatted = codec.deserialize(serialized, ProtocolVersion.NEWEST_SUPPORTED);
        assertThat(formatted).isEqualTo(DATE_TIME);
    }

    @Test
    void deserializeParse() {
        LocalDateTime formatted = codec.parse("1534156736000");
        assertThat(formatted).isEqualTo(DATE_TIME);
    }

    @Test
    void serializeEncodesNull() {
        ByteBuffer buffer = codec.serialize(null, ProtocolVersion.NEWEST_SUPPORTED);
        assertThat(buffer).isNull();
    }

    @Test
    void serializeFormatsNull() {
        String formatted = codec.format(null);
        assertThat(formatted).isEqualTo("NULL");
    }

    @Test
    void deserializeDecodesNull() {
        LocalDateTime out = codec.deserialize(ByteBuffer.allocate(0), ProtocolVersion.NEWEST_SUPPORTED);
        assertThat(out).isNull();
    }

    @Test
    void deserializeEmptyString() {
        ByteBuffer buffer = ByteBuffer.wrap("".getBytes(StandardCharsets.UTF_8));
        LocalDateTime out = codec.deserialize(buffer, ProtocolVersion.NEWEST_SUPPORTED);
        assertThat(out).isNull();
    }

    @Test
    void deserializeNullString() {
        ByteBuffer buffer = ByteBuffer.wrap("NULL".getBytes());
        LocalDateTime out = codec.deserialize(buffer, ProtocolVersion.NEWEST_SUPPORTED);
        assertThat(out).isNull();
    }

    @Test
    void parseDecodesNull() {
        LocalDateTime out = codec.parse(null);
        assertThat(out).isNull();
    }

    @Test
    void parseEmptyString() {
        LocalDateTime out = codec.parse("");
        assertThat(out).isNull();
    }

    @Test
    void parseNullString() {
        LocalDateTime out = codec.parse("NULL");
        assertThat(out).isNull();
    }

    @Test
    void parseInvalidDateString() {
        String invalid = "Invalid datetime";

        assertThatExceptionOfType(InvalidTypeException.class).isThrownBy(() -> codec.parse(invalid));
    }

    @Test
    void deserializeInvalidDateString() {
        ByteBuffer invalid = ByteBuffer.wrap("Invalid date".getBytes());

        assertThatExceptionOfType(InvalidTypeException.class).isThrownBy(() -> codec.deserialize(invalid, ProtocolVersion.NEWEST_SUPPORTED));
    }
}