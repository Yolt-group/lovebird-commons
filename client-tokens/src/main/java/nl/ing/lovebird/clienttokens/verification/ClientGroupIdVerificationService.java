package nl.ing.lovebird.clienttokens.verification;

import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.AbstractClientToken;
import nl.ing.lovebird.clienttokens.verification.exception.MismatchedClientGroupIdAndClientTokenException;
import nl.ing.lovebird.clienttokens.verification.sema.MismatchedClientGroupIdAndClientTokenSemaEvent;
import nl.ing.lovebird.logging.SemaEventLogger;
import org.springframework.lang.Nullable;

import java.util.UUID;

/**
 * This service can verify whether a provided client-group-id matches the client-group-id claim of the
 * client-token.
 *
 * @see AbstractClientToken
 */
@Slf4j
public class ClientGroupIdVerificationService {

    private static final String MISMATCH_MESSAGE =
            "provided client-group-id doesn't match client-group-id in client-token, respectively: '%s' and '%s'";

    /**
     * Verifies that a client-token's subject matches a provided client-group-id.
     *
     * @param clientToken   the client-token with a client-group-id claim as subject
     * @param clientGroupId the client-group-id to match the client-token's subject claim against
     * @throws MismatchedClientGroupIdAndClientTokenException when provided client-group-id mismatches subject of client-token
     */
    public void verify(@Nullable AbstractClientToken clientToken, @Nullable UUID clientGroupId) {
        if (!match(clientToken, clientGroupId)) {
            SemaEventLogger.log(new MismatchedClientGroupIdAndClientTokenSemaEvent(clientGroupId, clientToken));
            String clientIdFromToken = (clientToken == null) ? "client-token-is-null" : clientToken.getClientGroupIdClaim().toString();
            throw new MismatchedClientGroupIdAndClientTokenException(String.format(MISMATCH_MESSAGE, clientGroupId, clientIdFromToken));
        }
    }

    private boolean match(AbstractClientToken clientToken, UUID clientGroupId) {
        if (clientToken == null) {
            return clientGroupId == null;
        }
        if (clientGroupId == null) {
            return clientToken.getClientGroupIdClaim() == null;
        }
        return clientGroupId.equals(clientToken.getClientGroupIdClaim());
    }

}
