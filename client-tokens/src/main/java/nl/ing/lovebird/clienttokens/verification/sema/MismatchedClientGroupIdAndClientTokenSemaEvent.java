package nl.ing.lovebird.clienttokens.verification.sema;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.marker.Markers;
import nl.ing.lovebird.clienttokens.AbstractClientToken;
import nl.ing.lovebird.logging.SemaEvent;
import org.slf4j.Marker;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class MismatchedClientGroupIdAndClientTokenSemaEvent implements SemaEvent {

    private static final String MESSAGE =
            "provided client-group-id does not match client-group-id in client-group-token - client-group-id: %s; client-group-token: %s";

    private final UUID clientGroupId;
    private final AbstractClientToken clientToken;

    @Override
    public String getMessage() {
        return String.format(MESSAGE, clientGroupId, clientToken == null ? "client-token-is-null" : clientToken.getSerialized());
    }

    @Override
    public Marker getMarkers() {
        Map<String, String> entries = new HashMap<>();
        entries.put("client-group-id", clientGroupId == null ? "client-group-id-is-null" : clientGroupId.toString());
        entries.put("client-group-token-client-group-id", clientToken == null ? "client-token-is-null" : clientToken.getClientGroupIdClaim().toString());
        return Markers.appendEntries(entries);
    }
}
