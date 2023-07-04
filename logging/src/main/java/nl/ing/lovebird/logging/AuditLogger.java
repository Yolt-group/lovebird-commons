package nl.ing.lovebird.logging;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.marker.Markers;
import org.slf4j.Marker;

@Slf4j
public class AuditLogger {
    private static final String SUCCESS = "SUCCESS";
    private static final String ERROR = "ERROR";
    private static final String FAILURE = "FAILURE";
    private static final String RESULT_FIELD_NAME = "result";
    private static final String OBJECT_FIELD_NAME = "object";

    private static final AuditLogger INSTANCE = new AuditLogger();

    private final ObjectMapper mapper;

    public AuditLogger() {
        /*
         * We want to make sure that all fields of the objects are in the audit logs. That's why we don't want to depend
         * on toString or getters.
         */
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    /**
     * When the operation succeeds
     */
    public static void logSuccess(final String message, final Object object) {
        INSTANCE.logSuccessEvent(message, object);
    }

    /**
     * When the operation succeeds
     */
    public void logSuccessEvent(final String message, final Object object) {
        Marker marker = LogTypeMarker.getAuditMarker();
        marker.add(Markers.append(RESULT_FIELD_NAME, SUCCESS));
        if(object != null) {
            marker.add(Markers.append(OBJECT_FIELD_NAME, objectToJson(object)));
        }
        log.info(marker, message);
    }

    /**
     * When there is a logical error, i.e. operation is not possible at given circumstances
     */
    public static void logFailure(final String message, final Object object) {
        INSTANCE.logFailureEvent(message, object);
    }

    /**
     * When there is a logical error, i.e. operation is not possible at given circumstances
     */
    public void logFailureEvent(final String message, final Object object) {
        Marker marker = LogTypeMarker.getAuditMarker();
        marker.add(Markers.append(RESULT_FIELD_NAME, FAILURE));
        if (object != null) {
            marker.add(Markers.append(OBJECT_FIELD_NAME, objectToJson(object)));
        }
        log.warn(marker, message);
    }

    /**
     * Technical error occurred
     */
    public static void logError(final String message, final Object object, final Throwable throwable) {
        INSTANCE.logErrorEvent(message, object, throwable);
    }

    /**
     * Technical error occurred
     */
    public void logErrorEvent(final String message, final Object object, final Throwable throwable) {
        Marker marker = LogTypeMarker.getAuditMarker();
        marker.add(Markers.append(RESULT_FIELD_NAME, ERROR));
        if (object != null) {
            marker.add(Markers.append(OBJECT_FIELD_NAME, objectToJson(object)));
        }
        log.error(marker, message, throwable);
    }

    private String objectToJson(Object object) {
        try {
            final ObjectWriter writer = mapper.writer().without(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return writer.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert object of type {} to json for audit logging. Falling back to toString instead. Exception message was {}",
                    object.getClass().getName(),
                    e.getMessage());
            return object.toString();
        }
    }

}
