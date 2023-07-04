package nl.ing.lovebird.cassandra.codec;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.exceptions.InvalidTypeException;

import java.nio.ByteBuffer;
import java.util.Currency;

public class CurrencyTypeCodec extends TypeCodec<Currency> {

    @SuppressWarnings("WeakerAccess")
    public CurrencyTypeCodec() {
        super(DataType.text(), Currency.class);
    }

    @Override
    public ByteBuffer serialize(Currency value, ProtocolVersion protocolVersion) throws InvalidTypeException {
        if (value == null) {
            return null;
        }

        return TypeCodec.varchar().serialize(value.getCurrencyCode(), protocolVersion);
    }

    @Override
    public Currency deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) throws InvalidTypeException {
        if (bytes == null || bytes.remaining() == 0) {
            return null;
        }

        final String value = TypeCodec.varchar().deserialize(bytes, protocolVersion);

        if (value == null || value.isEmpty() || value.equals("NULL")) {
            return null;
        }

        try {
            return Currency.getInstance(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidTypeException(e.getMessage(), e);
        }
    }

    @Override
    public Currency parse(String value) throws InvalidTypeException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String format(Currency value) throws InvalidTypeException {
        throw new UnsupportedOperationException("Not implemented");
    }
}
