package nl.ing.lovebird.clienttokens.verification.sema;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.marker.Markers;
import nl.ing.lovebird.logging.SemaEvent;
import org.slf4j.Marker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class ClientTokenMissingSemaEvent implements SemaEvent {
    private static final String MESSAGE =
            "client-token missing: restricted-to: %s; client-id-header: %s;";

    private final String clientId;
    private final List<String> restrictedToList;

    @Override
    public String getMessage() {
        return String.format(MESSAGE, restrictedToList, clientId);
    }

    @Override
    public Marker getMarkers() {
        Map<String, Object> markers = new HashMap<>();
        markers.put("client-id-header", clientId);
        markers.put("restricted-to", restrictedToList.toString());
        return Markers.appendEntries(markers);
    }
}