package nl.ing.lovebird.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.logging.test.CaptureLogEvents;
import nl.ing.lovebird.logging.test.LogEvents;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CaptureLogEvents
class AuditLoggerTest {
    private final ExampleObject mockObject = new ExampleObject(true, 10);

    @Test
    void testSuccessEvent(LogEvents events) {
        AuditLogger.logSuccess("Mock message", mockObject);
        ILoggingEvent event = findFirstEvent(events, Level.INFO);
        assertThat(event.getMessage()).isEqualTo("Mock message");
        assertThat(event.getMarker().toString()).isEqualTo("log_type=AUDIT, result=SUCCESS, object={\"aBoolean\":true,\"anInteger\":10}");
    }

    @Test
    void testFailureEvent(LogEvents events) {
        AuditLogger.logFailure("Mock message", mockObject);
        ILoggingEvent event = findFirstEvent(events, Level.WARN);
        assertThat(event.getMessage()).isEqualTo("Mock message");
        assertThat(event.getMarker().toString()).isEqualTo("log_type=AUDIT, result=FAILURE, object={\"aBoolean\":true,\"anInteger\":10}");

    }

    @Test
    void testErrorEvent(LogEvents events) {
        AuditLogger.logError("Mock message", mockObject, new Exception("Mock exception message"));
        ILoggingEvent event = findFirstEvent(events, Level.ERROR);
        assertThat(event.getMessage()).isEqualTo("Mock message");
        assertThat(event.getMarker().toString()).isEqualTo("log_type=AUDIT, result=ERROR, object={\"aBoolean\":true,\"anInteger\":10}, error_code=4b3f764a");
        assertThat(event.getThrowableProxy().getMessage()).isEqualTo("Mock exception message");
    }

    @Test
    void testListOfObjects(LogEvents events) {
        AuditLogger.logSuccess("Mock message", Arrays.asList(mockObject, mockObject));
        ILoggingEvent event = findFirstEvent(events, Level.INFO);
        assertThat(event.getMessage()).isEqualTo("Mock message");
        assertThat(event.getMarker().toString()).endsWith("[{\"aBoolean\":true,\"anInteger\":10},{\"aBoolean\":true,\"anInteger\":10}]");

    }

    @Test
    void testObjectWithListOfObjects(LogEvents events) {
        AuditLogger.logSuccess("Mock message", new ExampleObjectWithList(Arrays.asList(mockObject, mockObject), false));
        ILoggingEvent event = findFirstEvent(events, Level.INFO);
        assertThat(event.getMessage()).isEqualTo("Mock message");
        assertThat(event.getMarker().toString()).endsWith("{\"exampleObjects\":[{\"aBoolean\":true,\"anInteger\":10},{\"aBoolean\":true,\"anInteger\":10}],\"aBoolean\":false}");

    }

    @Test
    void test3LayersDeep(LogEvents events) {
        AuditLogger.logSuccess("Mock message", new DeepExampleObject(new ExampleObjectWithList(Arrays.asList(mockObject, mockObject), false)));
        ILoggingEvent event = findFirstEvent(events, Level.INFO);
        assertThat(event.getMessage()).isEqualTo("Mock message");
        assertThat(event.getMarker().toString()).endsWith("{\"exampleObjects\":{\"exampleObjects\":[{\"aBoolean\":true,\"anInteger\":10},{\"aBoolean\":true,\"anInteger\":10}],\"aBoolean\":false}}");
    }

    @Test
    void testOnlyFieldsArePrintedAndNoGetters(LogEvents events) {
        AuditLogger.logSuccess("Mock message", new ExampleObjectWithGetters(false, 234));
        ILoggingEvent event = findFirstEvent(events, Level.INFO);
        assertThat(event.getMessage()).isEqualTo("Mock message");
        assertThat(event.getMarker().toString()).endsWith("{\"aBoolean\":false,\"anInteger\":234}");

    }

    @Test
    void testFallbackToToString(LogEvents events) {
        AuditLogger.logSuccess("Mock message", new ClassThatJacksonCannotSerialize());
        ILoggingEvent event = findFirstEvent(events, Level.INFO);
        assertThat(event.getMessage()).isEqualTo("Mock message");
        assertThat(event.getMarker().toString()).isEqualTo("log_type=AUDIT, result=SUCCESS, object=nl.ing.lovebird.logging.AuditLoggerTest$ClassThatJacksonCannotSerialize");

    }

    @Test
    void testNull(LogEvents events) {
        AuditLogger.logSuccess("Mock message", null);
        ILoggingEvent event = findFirstEvent(events, Level.INFO);
        assertThat(event.getMessage()).isEqualTo("Mock message");
        ;
        assertThat(event.getMarker().toString()).isEqualTo("log_type=AUDIT, result=SUCCESS");

    }

    @Test
    void testString(LogEvents events) {
        AuditLogger.logSuccess("Mock message", "hello world");
        ILoggingEvent event = findFirstEvent(events, Level.INFO);
        assertThat(event.getMessage()).isEqualTo("Mock message");
        assertThat(event.getMarker().toString()).isEqualTo("log_type=AUDIT, result=SUCCESS, object=\"hello world\"");
    }

    @Test
    void testTimestamp(LogEvents events) {
        AuditLogger.logSuccess("Mock message", new ClassWithJsr310Field(LocalDateTime.of(2020, Month.JUNE, 7, 13, 49, 0)));
        ILoggingEvent event = findFirstEvent(events, Level.INFO);
        assertThat(event.getMessage()).isEqualTo("Mock message");
        assertThat(event.getMarker().toString()).endsWith("{\"timestamp\":\"2020-06-07T13:49:00\"}");

    }

    private ILoggingEvent findFirstEvent(LogEvents events, Level info) {
        return events.stream(AuditLogger.class, info).findFirst().get();
    }

    @AllArgsConstructor
    private static class ExampleObject {
        private boolean aBoolean;
        private Integer anInteger;
    }

    @AllArgsConstructor
    private static class ExampleObjectWithList {
        private List<ExampleObject> exampleObjects;
        boolean aBoolean;
    }

    @AllArgsConstructor
    private static class DeepExampleObject {
        private final ExampleObjectWithList exampleObjects;
    }

    @Getter
    @RequiredArgsConstructor
    private static class ExampleObjectWithGetters {
        private final boolean aBoolean;
        private final Integer anInteger;

        public String getSomething() {
            return "This will not be printed.";
        }

    }

    private static class ClassThatJacksonCannotSerialize {
        private final ClassThatJacksonCannotSerialize self = this;

        @Override
        public String toString() {
            return self.getClass().getName();
        }
    }

    @AllArgsConstructor
    private static class ClassWithJsr310Field {
        private final LocalDateTime timestamp;
    }

}
