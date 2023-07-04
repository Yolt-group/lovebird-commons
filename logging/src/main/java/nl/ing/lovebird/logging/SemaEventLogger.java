package nl.ing.lovebird.logging;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.marker.Markers;
import org.slf4j.Marker;

/**
 * SEM-A: Security Event Monitoring: Application
 * SEM-A is an ING implementation of scanning application events for security issues
 * The log files this logger produces are sent to the ING monitoring tool
 */
@Slf4j
public class SemaEventLogger {

    private static final SemaEventLogger INSTANCE = new SemaEventLogger();

    public void logEvent(final SemaEvent event) {
        Marker marker = LogTypeMarker.getSemAMarker();
        marker.add(Markers.append("sema_type", event.getClass().getName()));
        if (event.getMarkers() != null) {
            marker.add(event.getMarkers());
        }

        log.info(marker, event.getMessage());
    }

    public static void log(final SemaEvent event) {
        INSTANCE.logEvent(event);
    }

}
