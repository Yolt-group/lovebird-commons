package nl.ing.lovebird.clienttokens.verification.sema;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.marker.Markers;
import nl.ing.lovebird.logging.SemaEvent;
import org.jose4j.jwt.NumericDate;
import org.slf4j.Marker;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class ClientTokenExpiredSemaEvent implements SemaEvent {
    private static final String MESSAGE =
            "client-token expired: issued-at: %s; expired-at: %s; issued-for: %s; user-id: %s; client-user-id: %s; client-id: %s; client-group-id: %s;";

    private final NumericDate issuedAt;
    private final NumericDate expiredAt;
    private final String issuedForClaim;
    private final UUID clientGroupId;
    private final UUID clientId;
    private final UUID clientUserId;
    private final UUID userId;

    @Override
    public String getMessage() {
        return String.format(MESSAGE, issuedAt.getValue(), expiredAt.getValue(), issuedForClaim, userId, clientUserId, clientId, clientGroupId);
    }

    @Override
    public Marker getMarkers() {
        Map<String, Object> markers = new HashMap<>();
        if (userId != null) {
            markers.put("user-id", userId.toString());
        }
        if (clientUserId != null) {
            markers.put("client-user-id", clientUserId.toString());
        }
        if (clientId != null) {
            markers.put("client-id", clientId.toString());
        }
        markers.put("client-group-id", clientGroupId.toString());
        markers.put("issued-for", issuedForClaim);
        markers.put("issued-at", issuedAt.getValue());
        markers.put("expired-at", expiredAt.getValue());
        return Markers.appendEntries(markers);
    }
}
