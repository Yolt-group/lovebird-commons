package nl.ing.lovebird.clienttokens.verification;

import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.verification.exception.MismatchedClientIdAndClientTokenException;
import nl.ing.lovebird.clienttokens.verification.sema.MismatchedClientIdAndClientTokenSemaEvent;
import nl.ing.lovebird.logging.SemaEventLogger;
import org.springframework.lang.Nullable;

import java.util.UUID;

/**
 * This service can verify whether a provided client-id matches the client-id claim of the
 * client-token.
 *
 * @see ClientToken
 */
@Slf4j
public class ClientIdVerificationService {

    private static final String MISMATCH_MESSAGE =
            "provided client-id doesn't match client-id in client-token, respectively: '%s' and '%s'";

    /**
     * Verifies that a client-token's subject matches a provided client-id.
     *
     * @param clientToken the client-token with a client-id claim as subject
     * @param clientId    the client-id to match the client-token's subject claim against
     * @throws MismatchedClientIdAndClientTokenException when provided client-id mismatches subject of client-token
     */
    public void verify(@Nullable ClientToken clientToken, @Nullable UUID clientId) {
        if (!match(clientToken, clientId)) {
            SemaEventLogger.log(new MismatchedClientIdAndClientTokenSemaEvent(clientId, clientToken));
            String clientIdFromToken = (clientToken == null) ? "client-token-is-null" : clientToken.getClientIdClaim().toString();
            throw new MismatchedClientIdAndClientTokenException(String.format(MISMATCH_MESSAGE, clientId, clientIdFromToken));
        }
    }

    private boolean match(ClientToken clientToken, UUID clientId) {
        if (clientToken == null) {
            return clientId == null;
        }
        if (clientId == null) {
            return clientToken.getClientIdClaim() == null;
        }
        return clientId.equals(clientToken.getClientIdClaim());
    }

}
