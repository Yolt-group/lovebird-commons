package nl.ing.lovebird.cassandra.codec;

import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.exceptions.InvalidTypeException;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class LocalDateTypeCodecTest {
    private static final LocalDate DATE = LocalDate.of(2018, 2, 1);
    private final LocalDateTypeCodec codec = new LocalDateTypeCodec();

    @Test
    void serialize() {
        ByteBuffer formatted = codec.serialize(DATE, ProtocolVersion.NEWEST_SUPPORTED);
        assertThat(new String(formatted.array())).isEqualTo("2018-02-01");
    }

    @Test
    void serializeFormat() {
        final String formatted = codec.format(DATE);
        assertThat(formatted).isEqualToIgnoringCase("2018-02-01");
    }


    @Test
    void deserialize() {
        ByteBuffer in = ByteBuffer.wrap("2018-02-01".getBytes());
        LocalDate formatted = codec.deserialize(in, ProtocolVersion.NEWEST_SUPPORTED);
        assertThat(formatted).isEqualTo(DATE);
    }

    @Test
    void deserializeParse() {
        String in = "2018-02-01";
        LocalDate formatted = codec.parse(in);
        assertThat(formatted).isEqualTo(DATE);
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
        LocalDate out = codec.deserialize(ByteBuffer.allocate(0), ProtocolVersion.NEWEST_SUPPORTED);
        assertThat(out).isNull();
    }

    @Test
    void deserializeEmptyString() {
        ByteBuffer buffer = ByteBuffer.wrap("".getBytes(StandardCharsets.UTF_8));
        LocalDate out = codec.deserialize(buffer, ProtocolVersion.NEWEST_SUPPORTED);
        assertThat(out).isNull();
    }

    @Test
    void deserializeNullString() {
        ByteBuffer buffer = ByteBuffer.wrap("NULL".getBytes());
        LocalDate out = codec.deserialize(buffer, ProtocolVersion.NEWEST_SUPPORTED);
        assertThat(out).isNull();
    }

    @Test
    void parseDecodesNull() {
        LocalDate out = codec.parse(null);
        assertThat(out).isNull();
    }

    @Test
    void parseEmptyString() {
        LocalDate out = codec.parse("");
        assertThat(out).isNull();
    }

    @Test
    void parseNullString() {
        LocalDate out = codec.parse("NULL");
        assertThat(out).isNull();
    }

    @Test
    void parseInvalidDateString() {
        String invalid = "Invalid date";

        assertThatExceptionOfType(InvalidTypeException.class).isThrownBy(() -> {
            codec.parse(invalid);
        });
    }

    @Test
    void deserializeInvalidDateString() {
        ByteBuffer invalid = ByteBuffer.wrap("Invalid date".getBytes());

        assertThatExceptionOfType(InvalidTypeException.class).isThrownBy(() -> {
            codec.deserialize(invalid, ProtocolVersion.NEWEST_SUPPORTED);
        });
    }
}