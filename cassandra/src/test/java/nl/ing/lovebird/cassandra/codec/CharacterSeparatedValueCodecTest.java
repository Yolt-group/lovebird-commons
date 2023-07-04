package nl.ing.lovebird.cassandra.codec;

import com.datastax.driver.core.ProtocolVersion;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class CharacterSeparatedValueCodecTest {
    private final CharacterSeparatedValueCodec codec = new CharacterSeparatedValueCodec();

    @Test
    void encodesNull() {
        Set<String> input = null;

        ByteBuffer buffer = codec.serialize(input, ProtocolVersion.NEWEST_SUPPORTED);
        String formatted = codec.format(input);

        assertThat(buffer).isNull();
        assertThat(formatted).isEqualTo("NULL");
    }

    @Test
    void encodesEmptySet() {
        Set<String> input = Collections.emptySet();

        ByteBuffer buffer = codec.serialize(input, ProtocolVersion.NEWEST_SUPPORTED);
        String formatted = codec.format(input);

        assertThat(buffer.hasRemaining()).isFalse();
        assertThat(formatted).isEmpty();
    }

    @Test
    void encodesSetWithOneValue() {
        Set<String> input = Collections.singleton("#foo");

        ByteBuffer buffer = codec.serialize(input, ProtocolVersion.NEWEST_SUPPORTED);
        String formatted = codec.format(input);

        assertThat(buffer.hasRemaining()).isTrue();
        assertThat(new String(buffer.array())).isEqualTo("#foo");
        assertThat(formatted).isEqualTo("#foo");
    }

    @Test
    void encodesSetWithMultipleValues() {
        String emoji = new String(Character.toChars(128521)); // :wink:
        Set<String> input = new HashSet<>(Arrays.asList("#foo", "#bar", emoji));

        ByteBuffer buffer = codec.serialize(input, ProtocolVersion.NEWEST_SUPPORTED);
        String formatted = codec.format(input);

        assertThat(buffer.remaining()).isEqualTo(14);
        assertThat(buffer.array()).isEqualTo("#bar #foo \uD83D\uDE09".getBytes(UTF_8));
        assertThat(formatted).isEqualTo("#bar #foo \uD83D\uDE09");
    }

    @Test
    void decodesNull() {
        ByteBuffer buffer = ByteBuffer.allocate(0);
        String formatted = null;

        Set<String> output = codec.deserialize(buffer, ProtocolVersion.NEWEST_SUPPORTED);
        Set<String> parsed = codec.parse(formatted);

        assertThat(output).isEmpty();
        assertThat(parsed).isEmpty();
    }

    @Test
    void decodesEmptyString() {
        String formatted = "";
        ByteBuffer buffer = ByteBuffer.wrap(formatted.getBytes(UTF_8));

        Set<String> output = codec.deserialize(buffer, ProtocolVersion.NEWEST_SUPPORTED);
        Set<String> parsed = codec.parse(formatted);

        assertThat(output).isEmpty();
        assertThat(parsed).isEmpty();
    }

    @Test
    void decodesNullString() {
        String formatted = "NULL";
        ByteBuffer buffer = ByteBuffer.wrap(formatted.getBytes());

        Set<String> output = codec.deserialize(buffer, ProtocolVersion.NEWEST_SUPPORTED);
        Set<String> parsed = codec.parse(formatted);

        assertThat(output).isEmpty();
        assertThat(parsed).isEmpty();
    }

    @Test
    void decodesSingleValue() {
        String formatted = "#foo";
        ByteBuffer buffer = ByteBuffer.wrap(formatted.getBytes());

        Set<String> output = codec.deserialize(buffer, ProtocolVersion.NEWEST_SUPPORTED);
        Set<String> parsed = codec.parse(formatted);

        assertThat(output).contains("#foo");
        assertThat(parsed).contains("#foo");
    }

    @Test
    void decodesMultipleValues() {
        String formatted = "#bar #foo \uD83D\uDE09";
        ByteBuffer buffer = ByteBuffer.wrap(formatted.getBytes(UTF_8));

        Set<String> output = codec.deserialize(buffer, ProtocolVersion.NEWEST_SUPPORTED);
        Set<String> parsed = codec.parse(formatted);

        assertThat(output).containsExactly("#bar", "#foo", "\uD83D\uDE09");
        assertThat(parsed).containsExactly("#bar", "#foo", "\uD83D\uDE09");
    }
}
