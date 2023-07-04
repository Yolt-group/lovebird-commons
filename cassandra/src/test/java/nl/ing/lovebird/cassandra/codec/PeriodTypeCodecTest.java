package nl.ing.lovebird.cassandra.codec;

import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.exceptions.InvalidTypeException;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PeriodTypeCodecTest {

    private static final PeriodTypeCodec CODEC = new PeriodTypeCodec();

    @Test
    void testSerializeAndDeserialize() {
        final Period in = Period.parse("P1D");
        final ByteBuffer serialized
                = CODEC.serialize(in, ProtocolVersion.NEWEST_SUPPORTED);
        final Period deserialized
                = CODEC.deserialize(serialized, ProtocolVersion.NEWEST_SUPPORTED);

        assertThat(in).isEqualTo(deserialized);
    }

    @Test
    void testEmptySerializedValueDeserialize() {
        final ByteBuffer input = ByteBuffer.allocate(0);
        final Period deserialize = CODEC.deserialize(input, ProtocolVersion.NEWEST_SUPPORTED);

        assertThat(deserialize).isNull();
    }

    @Test
    void testInvalidSerializedValueDeserialize() {
        final ByteBuffer input = ByteBuffer.wrap("invalid".getBytes());

        assertThatExceptionOfType(InvalidTypeException.class).isThrownBy(() -> {
            CODEC.deserialize(input, ProtocolVersion.NEWEST_SUPPORTED);
        });
    }

    @Test
    void testParseThrowsException() {
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> {
            CODEC.parse("P1M");
        });
    }

    @Test
    void testFormatThrowsException() {
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> {
            CODEC.format(Period.ofMonths(1));
        });
    }
}