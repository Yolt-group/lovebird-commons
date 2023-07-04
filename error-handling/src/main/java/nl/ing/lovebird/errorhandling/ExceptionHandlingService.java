package nl.ing.lovebird.errorhandling;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;

import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;

@Slf4j
public class ExceptionHandlingService {

    private static final int MAX_EXCEPTION_MESSAGE_LENGTH = 4000;


    private final String prefix;

    public ExceptionHandlingService(String prefix) {
        this.prefix = requireNonNull(prefix, "prefix may not be null");
    }

    private static String clip(final String originalMessage) {
        if (originalMessage == null) {
            return null;
        }
        if (originalMessage.length() <= MAX_EXCEPTION_MESSAGE_LENGTH) {
            return originalMessage;
        }
        return originalMessage.substring(0, MAX_EXCEPTION_MESSAGE_LENGTH) + "...";
    }

    public ErrorDTO logAndConstruct(ErrorInfo error, Throwable t) {
        return logAndConstruct(Level.ERROR, error, t);
    }

    public ErrorDTO logAndConstruct(Level errorLogLevel, ErrorInfo error, Throwable t) {
        logThrowable(errorLogLevel, error, t);
        String message = error.getMessage();
        return new ErrorDTO(code(error), message);
    }

    public String code(ErrorInfo error) {
        return prefix + error.getCode();
    }

    private void logThrowable(Level errorLogLevel, ErrorInfo error, Throwable t) {
        final BiConsumer<String, Object[]> l;

        switch (errorLogLevel) {
            case TRACE:
                l = log::trace;
                break;
            case DEBUG:
                l = log::debug;
                break;
            case INFO:
                l = log::info;
                break;
            case WARN:
                l = log::warn;
                break;
            default:
                l = log::error;
        }

        l.accept("{} ({}): {}", new Object[]{error.getMessage(), code(error), clip(t.getMessage()), t});
    }
}
