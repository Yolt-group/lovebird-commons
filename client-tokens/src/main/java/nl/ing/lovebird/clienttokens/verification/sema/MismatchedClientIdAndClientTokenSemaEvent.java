package nl.ing.lovebird.clienttokens.verification.sema;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.marker.Markers;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.logging.SemaEvent;
import org.slf4j.Marker;

@RequiredArgsConstructor
@Slf4j
public class MismatchedClientIdAndClientTokenSemaEvent implements SemaEvent {

    private static final String MESSAGE =
            "provided client-id does not match client-id in client-token - client-id: %s; client-token: %s";

    private final UUID clientId;
    private final ClientToken clientToken;

    @Override
    public String getMessage() {
        return String.format(MESSAGE, clientId, clientToken.getSerialized());
    }

    @Override
    public Marker getMarkers() {
        Map<String, String> entries = new HashMap<>();
        entries.put("client-id", clientId.toString());
        entries.put("client-token-client-id", clientToken.getClientIdClaim().toString());
        return Markers.appendEntries(entries);
    }
}
