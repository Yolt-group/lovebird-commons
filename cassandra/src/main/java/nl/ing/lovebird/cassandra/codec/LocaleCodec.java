package nl.ing.lovebird.cassandra.codec;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ParseUtils;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.datastax.driver.core.utils.Bytes;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocaleCodec extends TypeCodec<Locale> {
    private static final Pattern LOCALE_PATTERN = Pattern.compile("^([a-z]{2})_([A-Z]{2})$");

    private static final String NULL = "NULL";
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public LocaleCodec() {
        super(DataType.varchar(), Locale.class);
    }

    public static Locale toLocale(final String localeString) {
        Matcher m = LOCALE_PATTERN.matcher(localeString);

        if (!m.matches()) {
            throw new InvalidTypeException("Locale string should match pattern " + LOCALE_PATTERN.pattern());
        }

        return new Locale(m.group(1), m.group(2));
    }

    @Override
    public Locale parse(String value) {
        if (value == null || value.isEmpty() || value.equalsIgnoreCase(NULL)) {
            return null;
        }
        // strip enclosing single quotes, if any
        if (ParseUtils.isQuoted(value)) {
            value = ParseUtils.unquote(value);
        }
        return toLocale(value);
    }

    @Override
    public String format(final Locale value) {
        return value == null ? null : value.toString();
    }

    @Override
    public ByteBuffer serialize(final Locale value, final ProtocolVersion protocolVersion) {
        return value == null ? null : ByteBuffer.wrap(value.toString().getBytes(UTF_8));
    }

    @Override
    public Locale deserialize(final ByteBuffer bytes, final ProtocolVersion protocolVersion) {
        if (bytes == null) {
            return null;
        }
        if (bytes.remaining() == 0) {
            return null;
        }
        return toLocale(new String(Bytes.getArray(bytes), UTF_8));
    }
}
