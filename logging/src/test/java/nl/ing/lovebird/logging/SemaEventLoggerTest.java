package nl.ing.lovebird.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import nl.ing.lovebird.logging.test.CaptureLogEvents;
import nl.ing.lovebird.logging.test.LogEvents;
import org.junit.jupiter.api.Test;
import org.slf4j.Marker;

import static org.assertj.core.api.Assertions.assertThat;

@CaptureLogEvents
class SemaEventLoggerTest {

    private final SemaEventLogger semaEventLogger = new SemaEventLogger();

    @Test
    void testEvent(LogEvents events) {
        semaEventLogger.logEvent(new TestSemaEvent());
        ILoggingEvent event = events.stream(SemaEventLogger.class, Level.INFO).findFirst().get();
        assertThat(event.getMessage()).isEqualTo("Mock message");
        assertThat(event.getMarker().toString()).isEqualTo("log_type=SEMA, sema_type=nl.ing.lovebird.logging.TestSemaEvent, some_key=some_value, some_other_key=some_other_value");
    }

    @Test
    void testEventNullMarker(LogEvents events) {
        semaEventLogger.logEvent(new SemaEvent() {
            @Override
            public String getMessage() {
                return "Test message";
            }

            @Override
            public Marker getMarkers() {
                return null;
            }
        });

        ILoggingEvent event = events.stream(SemaEventLogger.class, Level.INFO).findFirst().get();
        assertThat(event.getMessage()).isEqualTo("Test message");
        assertThat(event.getMarker().toString()).isEqualTo("log_type=SEMA, sema_type=nl.ing.lovebird.logging.SemaEventLoggerTest$1");
    }

}
