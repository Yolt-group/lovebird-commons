package nl.ing.lovebird.cassandra.codec;

import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.exceptions.InvalidTypeException;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class LocaleCodecTest {

    private static final LocaleCodec CODEC = new LocaleCodec();

    @Test
    void testSerializeAndDeserialize() {
        final Locale in = Locale.UK;
        final ByteBuffer serialized
                = CODEC.serialize(in, ProtocolVersion.NEWEST_SUPPORTED);
        final Locale deserialized
                = CODEC.deserialize(serialized, ProtocolVersion.NEWEST_SUPPORTED);

        assertThat(in).isEqualTo(deserialized);
    }

    @Test
    void testEmptySerializedValueDeserialize() {
        final ByteBuffer input = ByteBuffer.allocate(0);
        final Locale deserialize = CODEC.deserialize(input, ProtocolVersion.NEWEST_SUPPORTED);

        assertThat(deserialize).isNull();
    }

    @Test
    void testInvalidSerializedValueDeserialize() {
        final ByteBuffer input = ByteBuffer.wrap("invalid".getBytes());

        assertThatExceptionOfType(InvalidTypeException.class).isThrownBy(() -> CODEC.deserialize(input, ProtocolVersion.NEWEST_SUPPORTED));
    }

    @Test
    void testParseThrowsException() {
        assertThat(CODEC.parse("nl_NL")).isEqualTo(new Locale("nl", "NL"));

    }

    @Test
    void testFormatThrowsException() {
        assertThat(CODEC.format(Locale.FRANCE)).isEqualTo("fr_FR");
    }
}
