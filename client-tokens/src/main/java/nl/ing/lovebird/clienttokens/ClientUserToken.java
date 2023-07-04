package nl.ing.lovebird.clienttokens;

import nl.ing.lovebird.clienttokens.constants.ClientTokenConstants;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;

import java.util.UUID;

public class ClientUserToken extends ClientToken {
    public ClientUserToken(String serialized, JwtClaims claims) {
        super(serialized, claims);
    }

    public UUID getUserIdClaim() {
        try {
            return UUID.fromString(
                    claims.getStringClaimValue(ClientTokenConstants.EXTRA_CLAIM_USER_ID)
            );
        } catch (MalformedClaimException | IllegalArgumentException e) {
            throw new IllegalStateException(e);
        }
    }

    public UUID getClientUserIdClaim() {
        try {
            return UUID.fromString(
                    claims.getStringClaimValue(ClientTokenConstants.EXTRA_CLAIM_CLIENT_USER_ID)
            );
        } catch (MalformedClaimException | IllegalArgumentException e) {
            throw new IllegalStateException(e);
        }
    }
}
