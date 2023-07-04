package nl.ing.lovebird.logging.test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.marker.Markers;
import nl.ing.lovebird.logging.AuditLogger;
import nl.ing.lovebird.logging.SemaEvent;
import nl.ing.lovebird.logging.SemaEventLogger;
import org.junit.jupiter.api.Test;
import org.slf4j.Marker;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Demo for CaptureLogEvents.
 * <p>
 * Sits here to prevent cycles.
 *
 * Note: {@code event.getMarker().toString()} really is the best way to test the contents of these markers.
 * The equals/hashcode implementation only checks the keys, but not the values.
 */
@Slf4j
@CaptureLogEvents
class CaptureLogEventsDemoTest {

    @Test
    void captureRegularLogEvents(LogEvents events) {
        log.info("Hello world");
        log.info("Hello sun");
        log.info("Hello moon");
        List<ILoggingEvent> event = events.stream(CaptureLogEventsDemoTest.class).collect(Collectors.toList());
        assertThat(event)
                .map(ILoggingEvent::getMessage)
                .containsExactly("Hello world", "Hello sun", "Hello moon");
    }

    @Test
    void captureAuditLogEvents(LogEvents events) {
        AuditLogger.logSuccess("We're in hack", Instant.ofEpochMilli(1655482201300L));
        ILoggingEvent event = events.stream(AuditLogger.class, Level.INFO).findFirst().get();
        assertThat(event.getMessage()).isEqualTo("We're in hack");
        assertThat(event.getMarker().toString()).isEqualTo("log_type=AUDIT, result=SUCCESS, object=\"2022-06-17T16:10:01.300Z\"");
    }

    @Test
    void captureSemEvents(LogEvents events) {
        SemaEventLogger.log(new TestSemaEvent());
        ILoggingEvent event = events.stream(SemaEventLogger.class, Level.INFO).findFirst().get();
        assertThat(event.getMessage()).isEqualTo("You're gonna need a bigger boat");
        assertThat(event.getMarker().toString()).isEqualTo("log_type=SEMA, sema_type=nl.ing.lovebird.logging.test.CaptureLogEventsDemoTest$TestSemaEvent, hello=there");
    }

    private static class TestSemaEvent implements SemaEvent {
        @Override
        public String getMessage() {
            return "You're gonna need a bigger boat";
        }

        @Override
        public Marker getMarkers() {
            return Markers.append("hello", "there");
        }
    }
}
