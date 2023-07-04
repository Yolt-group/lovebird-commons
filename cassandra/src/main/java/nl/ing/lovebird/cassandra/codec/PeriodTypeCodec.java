package nl.ing.lovebird.cassandra.codec;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.exceptions.InvalidTypeException;

import java.nio.ByteBuffer;
import java.time.Period;
import java.time.format.DateTimeParseException;

public class PeriodTypeCodec extends TypeCodec<Period> {

    @SuppressWarnings("WeakerAccess")
    public PeriodTypeCodec() {
        super(DataType.text(), Period.class);
    }

    @Override
    public ByteBuffer serialize(Period value, ProtocolVersion protocolVersion) throws InvalidTypeException {
        if (value == null) {
            return null;
        }

        return TypeCodec.varchar().serialize(value.toString(), protocolVersion);
    }

    @Override
    public Period deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) throws InvalidTypeException {
        if (bytes == null || bytes.remaining() == 0) {
            return null;
        }

        final String value = TypeCodec.varchar().deserialize(bytes, protocolVersion);

        if (value == null || value.isEmpty() || value.equals("NULL")) {
            return null;
        }

        try {
            return Period.parse(value);
        } catch (DateTimeParseException e) {
            throw new InvalidTypeException(e.getMessage(), e);
        }
    }

    @Override
    public Period parse(String value) throws InvalidTypeException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String format(Period value) throws InvalidTypeException {
        throw new UnsupportedOperationException("Not implemented");
    }
}
