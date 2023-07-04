package nl.ing.lovebird.logging;

import net.logstash.logback.marker.Markers;
import org.slf4j.Marker;

public class TestSemaEvent implements SemaEvent {
    @Override
    public String getMessage() {
        return "Mock message";
    }

    @Override
    public Marker getMarkers() {
        return Markers.append("some_key", "some_value")
                .and(Markers.append("some_other_key", "some_other_value"));
    }
}
