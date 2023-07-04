package nl.ing.lovebird.clienttokens;

import nl.ing.lovebird.clienttokens.constants.ClientTokenConstants;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;

import java.util.UUID;

public class ClientToken extends AbstractClientToken {

    public ClientToken(String serialized, JwtClaims claims) {
        super(serialized, claims);
    }

    public UUID getClientIdClaim() {
        try {
            if (claims.hasClaim(ClientTokenConstants.EXTRA_CLAIM_CLIENT_ID)) {
                return UUID.fromString(
                        claims.getStringClaimValue(ClientTokenConstants.EXTRA_CLAIM_CLIENT_ID)
                );
            } else {
                // Fallback on "sub" claim
                return UUID.fromString(
                        claims.getStringClaimValue(ClientTokenConstants.SUBJECT)
                );
            }
        } catch (MalformedClaimException | IllegalArgumentException e) {
            throw new IllegalStateException(e);
        }
    }

    public String getClientNameClaim() {
        try {
            return claims.getStringClaimValue(ClientTokenConstants.EXTRA_CLAIM_CLIENT_NAME);
        } catch (MalformedClaimException | IllegalArgumentException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean hasKYCForPrivateIndividuals() {
        return getBooleanClaim(ClientTokenConstants.CLAIM_CLIENT_USERS_KYC_PRIVATE_INDIVIDUALS);
    }

    public boolean hasKYCForEntities() {
        return getBooleanClaim(ClientTokenConstants.CLAIM_CLIENT_USERS_KYC_ENTITIES);
    }

    public boolean isPSD2Licensed() {
        return getBooleanClaim(ClientTokenConstants.CLAIM_PSD2_LICENSED);
    }

    public boolean hasAIS() {
        return getBooleanClaim(ClientTokenConstants.CLAIM_AIS);
    }

    public boolean hasPIS() {
        return getBooleanClaim(ClientTokenConstants.CLAIM_PIS);
    }

    public boolean hasCAM() {
        return getBooleanClaim(ClientTokenConstants.CLAIM_CAM);
    }

    public boolean hasOneOffAIS() {
        return getBooleanClaim(ClientTokenConstants.CLAIM_ONE_OFF_AIS);
    }

    public boolean hasConsentStarter() {
        return getBooleanClaim(ClientTokenConstants.CLAIM_CONSENT_STARTER);
    }

    public boolean hasRiskInsights() {
        return getBooleanClaim(ClientTokenConstants.CLAIM_RISK_INSIGHTS);
    }

    public boolean hasDataEnrichmentMerchantRecognition() {
        return getBooleanClaim(ClientTokenConstants.CLAIM_DATA_ENRICHMENT_MERCHANT_RECOGNITION);
    }

    public boolean hasDataEnrichmentCategorization() {
        return getBooleanClaim(ClientTokenConstants.CLAIM_DATA_ENRICHMENT_CATEGORIZATION);
    }

    public boolean hasDataEnrichmentCycleDetection() {
        return getBooleanClaim(ClientTokenConstants.CLAIM_DATA_ENRICHMENT_CYCLE_DETECTION);
    }

    public boolean hasDataEnrichmentLabels() {
        return getBooleanClaim(ClientTokenConstants.CLAIM_DATA_ENRICHMENT_LABELS);
    }

    public boolean isDeleted() {
        return getBooleanClaim(ClientTokenConstants.CLAIM_DELETED);
    }
}
