package nl.ing.lovebird.clienttokens.verification;

import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.AbstractClientToken;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.clienttokens.verification.exception.MismatchedUserIdAndClientTokenException;
import nl.ing.lovebird.clienttokens.verification.sema.MismatchedUserIdAndClientTokenSemaEvent;
import nl.ing.lovebird.logging.SemaEventLogger;
import org.springframework.lang.Nullable;

import java.util.UUID;

/**
 * This service can verify whether a provided user-id matches the user-id claim of the
 * client-token.
 *
 * @see AbstractClientToken
 */
@Slf4j
public class UserIdVerificationService {

    private static final String MISMATCH_MESSAGE =
            "provided user-id doesn't match user-id in client-token, respectively: '%s' and '%s'";

    /**
     * Verifies that a client-token's subject matches a provided client-group-id.
     *
     * @param clientToken  the client-token with a client-group-id claim as subject
     * @param userId the user-id to match the client-token's subject claim against
     * @throws MismatchedUserIdAndClientTokenException when provided user-id mismatches subject of client-token
     */
    public void verify(@Nullable ClientUserToken clientToken, @Nullable UUID userId) {
        if (!match(clientToken, userId)) {
            SemaEventLogger.log(new MismatchedUserIdAndClientTokenSemaEvent(userId, clientToken));
            String userIdFromToken = (clientToken == null) ? "client-token-is-null" : clientToken.getUserIdClaim().toString();
            throw new MismatchedUserIdAndClientTokenException(String.format(MISMATCH_MESSAGE, userId, userIdFromToken));
        }
    }

    private boolean match(ClientUserToken clientToken, UUID userId) {
        if (clientToken == null) {
            return userId == null;
        }
        if (userId == null) {
            return clientToken.getUserIdClaim() == null;
        }
        return userId.equals(clientToken.getUserIdClaim());
    }

}
