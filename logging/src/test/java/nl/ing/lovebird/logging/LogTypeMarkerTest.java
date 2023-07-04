package nl.ing.lovebird.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.logging.test.CaptureLogEvents;
import nl.ing.lovebird.logging.test.LogEvents;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@CaptureLogEvents
class LogTypeMarkerTest {

    @Test
    void testGetAuditMarker(LogEvents events) {
        log.info(LogTypeMarker.getAuditMarker(), "Works like a charm!");
        ILoggingEvent event = events.stream(LogTypeMarkerTest.class, Level.INFO).findFirst().get();
        assertThat(event.getMarker().toString()).isEqualTo("log_type=AUDIT");
    }

    @Test
    void testGetServiceCallMarker(LogEvents events) {
        log.info(LogTypeMarker.getServiceCallMarker(), "Works like a charm!");
        ILoggingEvent event = events.stream(LogTypeMarkerTest.class, Level.INFO).findFirst().get();
        assertThat(event.getMarker().toString()).isEqualTo("log_type=SERVICE_CALL");
    }

    @Test
    void testGetKafkaCallMarker(LogEvents events) {
        log.info(LogTypeMarker.getKafkaCallMarker(), "Works like a charm!");
        ILoggingEvent event = events.stream(LogTypeMarkerTest.class, Level.INFO).findFirst().get();
        assertThat(event.getMarker().toString()).isEqualTo("log_type=KAFKA_CALL");
    }

    @Test
    void testGetDataErrorMarker(LogEvents events) {
        log.warn(LogTypeMarker.getDataErrorMarker(), "Works like a charm!");
        ILoggingEvent event = events.stream(LogTypeMarkerTest.class, Level.WARN).findFirst().get();
        assertThat(event.getMarker().toString()).isEqualTo("log_type=DATA_ERROR");
    }

}
