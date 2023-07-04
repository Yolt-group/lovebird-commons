package nl.ing.lovebird.clienttokens.verification.sema;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.marker.Markers;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.logging.SemaEvent;
import org.slf4j.Marker;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class MismatchedUserIdAndClientTokenSemaEvent implements SemaEvent {

    private static final String MESSAGE =
            "provided user-id does not match user-id in client-token - user-id: %s; client-token: %s";

    private final UUID userId;
    private final ClientUserToken clientToken;

    @Override
    public String getMessage() {
        return String.format(MESSAGE, userId, clientToken.getSerialized());
    }

    @Override
    public Marker getMarkers() {
        Map<String, String> entries = new HashMap<>();
        entries.put("user-id", userId.toString());
        entries.put("client-token-user-id", clientToken.getUserIdClaim().toString());
        return Markers.appendEntries(entries);
    }
}
