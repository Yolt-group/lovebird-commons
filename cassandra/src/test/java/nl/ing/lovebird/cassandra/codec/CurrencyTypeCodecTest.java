package nl.ing.lovebird.cassandra.codec;

import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.exceptions.InvalidTypeException;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CurrencyTypeCodecTest {

    private static final CurrencyTypeCodec CODEC = new CurrencyTypeCodec();

    @Test
    void testSerializeAndDeserialize() {
        final Currency in = Currency.getInstance("EUR");
        final ByteBuffer serialized
                = CODEC.serialize(in, ProtocolVersion.NEWEST_SUPPORTED);
        final Currency deserialized
                = CODEC.deserialize(serialized, ProtocolVersion.NEWEST_SUPPORTED);

        assertThat(in).isEqualTo(deserialized);
    }

    @Test
    void testEmptySerializedValueDeserialize() {
        final ByteBuffer input = ByteBuffer.allocate(0);
        final Currency deserialize = CODEC.deserialize(input, ProtocolVersion.NEWEST_SUPPORTED);

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
            CODEC.parse("EUR");
        });
    }

    @Test
    void testFormatThrowsException() {
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> {
            CODEC.format(Currency.getInstance("EUR"));
        });
    }
}