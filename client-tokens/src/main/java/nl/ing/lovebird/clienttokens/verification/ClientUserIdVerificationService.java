package nl.ing.lovebird.clienttokens.verification;

import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.AbstractClientToken;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.clienttokens.verification.exception.MismatchedClientUserIdAndClientTokenException;
import nl.ing.lovebird.clienttokens.verification.sema.MismatchedClientUserIdAndClientTokenSemaEvent;
import nl.ing.lovebird.logging.SemaEventLogger;
import org.springframework.lang.Nullable;

import java.util.UUID;

/**
 * This service can verify whether a provided client-user-id matches the client-user-id claim of the
 * client-token.
 *
 * @see AbstractClientToken
 */
@Slf4j
public class ClientUserIdVerificationService {

    private static final String MISMATCH_MESSAGE =
            "provided client-user-id doesn't match client-user-id in client-token, respectively: '%s' and '%s'";

    /**
     * Verifies that a client-token's subject matches a provided client-group-id.
     *
     * @param clientToken  the client-token with a client-group-id claim as subject
     * @param clientUserId the client-user-id to match the client-token's subject claim against
     * @throws MismatchedClientUserIdAndClientTokenException when provided client-user-id mismatches subject of client-token
     */
    public void verify(@Nullable ClientUserToken clientToken, @Nullable UUID clientUserId) {
        if (!match(clientToken, clientUserId)) {
            SemaEventLogger.log(new MismatchedClientUserIdAndClientTokenSemaEvent(clientUserId, clientToken));
            String clientUserIdFromToken = (clientToken == null) ? "client-token-is-null" : clientToken.getClientUserIdClaim().toString();
            throw new MismatchedClientUserIdAndClientTokenException(String.format(MISMATCH_MESSAGE, clientUserId, clientUserIdFromToken));
        }
    }

    private boolean match(ClientUserToken clientToken, UUID clientUserId) {
        if (clientToken == null) {
            return clientUserId == null;
        }
        if (clientUserId == null) {
            return clientToken.getClientUserIdClaim() == null;
        }
        return clientUserId.equals(clientToken.getClientUserIdClaim());
    }

}
