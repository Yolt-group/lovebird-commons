package nl.ing.lovebird.cassandra.codec;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.exceptions.InvalidTypeException;

import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateTypeCodec extends TypeCodec<LocalDate> {

    private static final DateTimeFormatter ISO_LOCAL_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @SuppressWarnings("WeakerAccess")
    public LocalDateTypeCodec() {
        super(DataType.text(), LocalDate.class);
    }

    @Override
    public ByteBuffer serialize(LocalDate value, ProtocolVersion protocolVersion) throws InvalidTypeException {
        if (value == null) {
            return null;
        }

        return TypeCodec.varchar().serialize(value.format(ISO_LOCAL_DATE_FORMATTER), protocolVersion);
    }

    @Override
    public LocalDate deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) throws InvalidTypeException {

        final String value = TypeCodec.varchar().deserialize(bytes, protocolVersion);

        if (value == null || value.isEmpty() || value.equals("NULL")) {
            return null;
        }

        try {
            return LocalDate.parse(value, ISO_LOCAL_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidTypeException(e.getMessage(), e);
        }
    }

    @Override
    public LocalDate parse(String value) throws InvalidTypeException {
        if (value == null || value.isEmpty() || value.equals("NULL")) {
            return null;
        }

        try {
            return LocalDate.parse(value, ISO_LOCAL_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidTypeException(e.getMessage(), e);
        }
    }

    @Override
    public String format(LocalDate value) throws InvalidTypeException {
        if (value == null) {
            return "NULL";
        }

        return value.format(ISO_LOCAL_DATE_FORMATTER);
    }
}
