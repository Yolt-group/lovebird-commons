package nl.ing.lovebird.cassandra.codec;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ParseUtils;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.exceptions.InvalidTypeException;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDateTime;

import static java.time.ZoneOffset.UTC;

public class LocalDateTimeCodec extends TypeCodec<LocalDateTime> {

    public static final String NULL = "NULL";

    public LocalDateTimeCodec() {
        super(DataType.timestamp(), LocalDateTime.class);
    }

    @Override
    public LocalDateTime parse(String value) {
        if (value == null || value.isEmpty() || value.equalsIgnoreCase(NULL)) {
            return null;
        }
        // strip enclosing single quotes, if any
        if (ParseUtils.isQuoted(value)) {
            value = ParseUtils.unquote(value);
        }

        try {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(value)), UTC);
        } catch (NumberFormatException e) {
            throw new InvalidTypeException(String.format("Cannot parse timestamp value from \"%s\"", value));
        }
    }

    @Override
    public String format(LocalDateTime value) {
        if (value == null) {
            return "NULL";
        }
        return Long.toString(value.toInstant(UTC).toEpochMilli());
    }

    @Override
    public ByteBuffer serialize(LocalDateTime value, ProtocolVersion protocolVersion) {
        return value == null ? null : BigintCodec.instance.serializeNoBoxing(value.toInstant(UTC).toEpochMilli(), protocolVersion);
    }

    @Override
    public LocalDateTime deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) {
        return bytes == null || bytes.remaining() == 0 || NULL.equals(new String(bytes.array(), Charset.forName("UTF-8")))
                ? null
                : LocalDateTime.ofInstant(Instant.ofEpochMilli(BigintCodec.instance.deserializeNoBoxing(bytes, protocolVersion)), UTC);
    }

    private static class BigintCodec extends LongCodec {

        static final BigintCodec instance = new BigintCodec();

        private BigintCodec() {
            super(DataType.bigint());
        }
    }

    private abstract static class LongCodec extends PrimitiveLongCodec {

        LongCodec(DataType cqlType) {
            super(cqlType);
        }

        @Override
        public Long parse(String value) {
            try {
                return value == null || value.isEmpty() || value.equalsIgnoreCase("NULL") ? null : Long.parseLong(value);
            } catch (NumberFormatException e) {
                throw new InvalidTypeException(String.format("Cannot parse 64-bits long value from \"%s\"", value));
            }
        }

        @Override
        public String format(Long value) {
            if (value == null) {
                return "NULL";
            }
            return Long.toString(value);
        }

        @Override
        public ByteBuffer serializeNoBoxing(long value, ProtocolVersion protocolVersion) {
            ByteBuffer bb = ByteBuffer.allocate(8);
            bb.putLong(0, value);
            return bb;
        }

        @Override
        public long deserializeNoBoxing(ByteBuffer bytes, ProtocolVersion protocolVersion) {
            if (bytes == null || bytes.remaining() == 0) {
                return 0;
            }
            if (bytes.remaining() != 8) {
                throw new InvalidTypeException("Invalid 64-bits long value, expecting 8 bytes but got " + bytes.remaining());
            }

            return bytes.getLong(bytes.position());
        }
    }
}
