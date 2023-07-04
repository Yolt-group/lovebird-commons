package nl.ing.lovebird.clienttokens;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.clienttokens.annotations.VerifiedClientToken;
import nl.ing.lovebird.clienttokens.constants.ClientTokenConstants;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Can be requested using ClientTokenRequesterService.
 * Can be injected using the {@link VerifiedClientToken} annotation.
 */
@RequiredArgsConstructor
@EqualsAndHashCode(of = "serialized")
public abstract class AbstractClientToken {

    /**
     * Compact serialization of the JWS representation of the client-token.
     * Can be used to propagate the client-token header in successive calls.
     */
    @Getter
    private final String serialized;

    /**
     * Claims of the client-token.
     */
    protected final JwtClaims claims;

    public UUID getClientGroupIdClaim() {
        try {
            return UUID.fromString(
                    claims.getStringClaimValue(ClientTokenConstants.EXTRA_CLAIM_CLIENT_GROUP_ID)
            );
        } catch (MalformedClaimException e) {
            throw new IllegalStateException(e);
        }
    }

    public String getIssuedForClaim() {
        try {
            String isf = claims.getStringClaimValue(ClientTokenConstants.EXTRA_CLAIM_ISSUED_FOR);
            if (isf == null || isf.isEmpty()) {
                throw new IllegalStateException("client-token claim '" + ClientTokenConstants.EXTRA_CLAIM_ISSUED_FOR + "' is null or empty");
            }
            return isf;
        } catch (MalformedClaimException e) {
            throw new IllegalStateException(e);
        }
    }

    public Map<String, Object> getClaimsMap() {
        return Collections.unmodifiableMap(claims.getClaimsMap());
    }

    public Object getClaimValue(String claimName) {
        return claims.getClaimValue(claimName);
    }

    public String getStringClaimValue(String claimName) throws MalformedClaimException {
        return claims.getStringClaimValue(claimName);
    }

    public <T> T getClaimValue(String claimName, Class<T> type) throws MalformedClaimException {
        return claims.getClaimValue(claimName, type);
    }

    public boolean hasClaim(String claimName) {
        return claims.hasClaim(claimName);
    }

    public String getIssuer() {
        try {
            return claims.getIssuer();
        } catch (MalformedClaimException e) {
            throw new IllegalStateException(e);
        }
    }

    public String getSubject() {
        try {
            return claims.getSubject();
        } catch (MalformedClaimException e) {
            throw new IllegalStateException(e);
        }
    }

    public NumericDate getExpirationTime() {
        try {
            return claims.getExpirationTime();
        } catch (MalformedClaimException e) {
            throw new IllegalStateException(e);
        }
    }

    public NumericDate getIssuedAt() {
        try {
            return claims.getIssuedAt();
        } catch (MalformedClaimException e) {
            throw new IllegalStateException(e);
        }
    }

    public String getJwtId() {
        try {
            return claims.getJwtId();
        } catch (MalformedClaimException e) {
            throw new IllegalStateException(e);
        }
    }

    protected boolean getBooleanClaim(String claim) {
        try {
            return Optional.ofNullable(claims.getClaimValue(claim, Boolean.class)).orElseThrow(() -> new IllegalStateException(String.format("The claim %s was not found in the client-token", claim)));
        } catch (MalformedClaimException e) {
            throw new IllegalStateException(e);
        }
    }

    public Collection<String> getClaimNames() {
        return new ArrayList<>(claims.getClaimNames());
    }

    /**
     * Returns a string representation of the client-token object. More specifically,
     * this returns the compact serialization of its JWS representation.
     *
     * Note: This method is used by kafka to serialize instances of this class.
     */
    @Override
    public String toString() {
        return this.serialized;
    }

    public String getClientGroupNameClaim() {
        try {
            return claims.getStringClaimValue(ClientTokenConstants.EXTRA_CLAIM_CLIENT_GROUP_NAME);
        } catch (MalformedClaimException | IllegalArgumentException e) {
            throw new IllegalStateException(e);
        }
    }
}
