package nl.ing.lovebird.cassandra.codec;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.TypeTokens;
import com.datastax.driver.core.exceptions.InvalidTypeException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Codec that stores a set as character separated values (CSV).
 */
public class CharacterSeparatedValueCodec extends TypeCodec<Set<String>> {
    public static final String SEPARATOR = " ";
    public static final String SEPARATOR_REGEX = SEPARATOR;

    public CharacterSeparatedValueCodec() {
        super(DataType.text(), TypeTokens.setOf(String.class));
    }

    @Override
    public ByteBuffer serialize(Set<String> value, ProtocolVersion protocolVersion) throws InvalidTypeException {
        if (value == null) {
            return null;
        }

        String joined = value.stream().collect(Collectors.joining(SEPARATOR));
        return TypeCodec.varchar().serialize(joined, protocolVersion);
    }

    @Override
    public Set<String> deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) throws InvalidTypeException {
        return new HashSet<>(decodeToElements(TypeCodec.varchar().deserialize(bytes, protocolVersion)));
    }

    @Override
    public Set<String> parse(String value) throws InvalidTypeException {
        return new HashSet<>(decodeToElements(value));
    }

    @Override
    public String format(Set<String> value) throws InvalidTypeException {
        if (value == null) {
            return "NULL";
        }

        return value.stream().collect(Collectors.joining(SEPARATOR));
    }

    private static List<String> decodeToElements(String values) {
        return Optional.ofNullable(values)
                .filter(s -> !s.isEmpty())
                .filter(s -> !s.equalsIgnoreCase("NULL"))
                .map(s -> Arrays.asList(s.split(SEPARATOR_REGEX)))
                .orElseGet(ArrayList::new);
    }
}
