package nl.ing.lovebird.logging;

import net.logstash.logback.marker.Markers;
import org.slf4j.Marker;

public class LogTypeMarker {

    private static final String LOG_TYPE_KEY = "log_type";

    private LogTypeMarker() {
    }

    /**
     * Package private: should not be used directly, but via the AuditLogger
     */
    static Marker getAuditMarker() {
        return LogTypeMarkerBuilder.builder()
                .withType(LogType.AUDIT)
                .build();
    }

    /**
     * Package private: should not be used directly, but via the SemaEventLogger
     */
    static Marker getSemAMarker() {
        return LogTypeMarkerBuilder.builder()
                .withType(LogType.SEMA)
                .build();
    }

    public static Marker getEndpointMarker() {
        return LogTypeMarkerBuilder.builder()
                .withType(LogType.ENDPOINT)
                .build();
    }

    public static Marker getServiceCallMarker() {
        return LogTypeMarkerBuilder.builder()
                .withType(LogType.SERVICE_CALL)
                .build();
    }

    public static Marker getKafkaCallMarker() {
        return LogTypeMarkerBuilder.builder()
                .withType(LogType.KAFKA_CALL)
                .build();
    }

    public static Marker getDataErrorMarker() {
        return LogTypeMarkerBuilder.builder()
                .withType(LogType.DATA_ERROR)
                .build();
    }

    public static Marker getServiceCallHealthMarker() {
        return LogTypeMarkerBuilder.builder()
                .withType(LogType.SERVICE_CALL_HEALTH)
                .build();
    }

    public static Marker getKafkaHealthMarker() {
        return LogTypeMarkerBuilder.builder()
                .withType(LogType.KAFKA_HEALTH)
                .build();
    }

    public static Marker getExternalErrorMarker() {
        return LogTypeMarkerBuilder.builder()
                .withType(LogType.EXTERNAL_ERROR)
                .build();
    }

    public static Marker getFrontEndMarker() {
        return LogTypeMarkerBuilder.builder()
                .withType(LogType.FRONT_END)
                .build();
    }

    private enum LogType {
        AUDIT,
        SEMA,
        ENDPOINT,
        SERVICE_CALL,
        SERVICE_CALL_HEALTH,
        KAFKA_CALL,
        KAFKA_HEALTH,
        EXTERNAL_ERROR,
        DATA_ERROR,
        FRONT_END
    }

    private static class LogTypeMarkerBuilder {
        private LogType logType;

        public static LogTypeMarkerBuilder builder() {
            return new LogTypeMarkerBuilder();
        }

        public LogTypeMarkerBuilder withType(final LogType logType) {
            this.logType = logType;
            return this;
        }

        public Marker build() {
            return Markers.append(LOG_TYPE_KEY, logType.name());
        }
    }
}
