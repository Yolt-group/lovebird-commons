package nl.ing.lovebird.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.marker.Markers;
import nl.ing.lovebird.logging.test.CaptureLogEvents;
import nl.ing.lovebird.logging.test.LogEvents;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@CaptureLogEvents
class AddErrorCodeFilterTest {

    @Test
    void testInfoIsNotAltered(LogEvents events) {
        log.info("Some info here: {}", "info!!");
        ILoggingEvent event = findFirstEvent(events, Level.INFO);
        assertThat(event.getMarker()).isNull();
    }

    @Test
    void testLongLogLine(LogEvents events) {
        StringBuilder builder = new StringBuilder(16 * 1024);
        for (int i = 0; i < 16 * 1024; i++) {
            builder.append(i % 10);
        }

        log.info("This generates multiple log lines: {}", builder);
        ILoggingEvent event = findFirstEvent(events, Level.INFO);
        assertThat(event.getMarker()).isNull();
    }

    @Test
    void testInfoIsNotAlteredWithExistingMarker(LogEvents events) {
        log.info(Markers.append("some", "marker"), "Some info here: {}", "info!!");
        ILoggingEvent event = findFirstEvent(events, Level.INFO);
        assertThat(event.getMarker().toString()).isEqualTo("some=marker");
    }

    @Test
    void testWarnIsNotAltered(LogEvents events) {
        log.warn("Some warn here: {}", "warn!!");
        ILoggingEvent event = findFirstEvent(events, Level.WARN);
        assertThat(event.getMarker()).isNull();
    }

    @Test
    void testErrorContainsErrorCode(LogEvents events) {
        log.error("Some error here: {}", "ERROR!!");
        ILoggingEvent event = findFirstEvent(events, Level.ERROR);
        assertThat(event.getMarker().toString()).isEqualTo("error_code=f6af289c");
    }

    @Test
    void testErrorContainsErrorCodeWithExistingMarker(LogEvents events) {
        log.error(Markers.append("some", "marker"), "Some error here: {}", "ERROR!!");
        ILoggingEvent event = findFirstEvent(events, Level.ERROR);
        assertThat(event.getMarker().toString()).isEqualTo("some=marker, error_code=f6af289c");
    }

    @Test
    void testGenericStackTraceError(LogEvents events) {
        IllegalStateException someException = new IllegalStateException("Some error message", new IllegalArgumentException("Oh no!"));
        log.error("Some unknown error: {}", someException.getMessage(), someException);
        ILoggingEvent event = findFirstEvent(events, Level.ERROR);
        assertThat(event.getMarker().toString()).isEqualTo("error_code=d5a23a92");
    }

    @Test
    void testGenericStackTraceErrorDifferentException(LogEvents events) {
        IllegalArgumentException someException = new IllegalArgumentException("Some error message", new IllegalArgumentException("Oh no!"));
        log.error("Some unknown error: {}", someException.getMessage(), someException);
        ILoggingEvent event = findFirstEvent(events, Level.ERROR);
        assertThat(event.getMarker().toString()).isEqualTo("error_code=5659d9be");
    }

    private ILoggingEvent findFirstEvent(LogEvents events, Level level) {
        return events.stream(AddErrorCodeFilterTest.class, level).findFirst().get();
    }

}
