package nl.ing.lovebird.clienttokens.test;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;

import java.util.UUID;

import static nl.ing.lovebird.clienttokens.constants.ClientTokenConstants.*;

/**
 * Creates a sensible collection of JWT claims for testing purposes.
 */
public class TestJwtClaims {

    private TestJwtClaims(){

    }

    public static JwtClaims createClientGroupClaims(String issueForService, UUID clientGroupId) {
        JwtClaims claims = createClaims(issueForService);
        claims.setClaim(SUBJECT, "group:" + clientGroupId.toString());
        claims.setClaim(EXTRA_CLAIM_CLIENT_GROUP_ID, clientGroupId.toString());
        return claims;
    }

    public static JwtClaims createClientClaims(String issueForService, UUID clientGroupId, UUID clientId) {
        JwtClaims claims = createClaims(issueForService);
        addClientClaims(claims, clientGroupId, clientId);
        claims.setClaim(SUBJECT, "client:" + clientId);
        return claims;
    }

    public static JwtClaims createClientUserClaims(String issueForService, UUID clientGroupId, UUID clientId, UUID userId) {
        JwtClaims claims = createClaims(issueForService);
        addClientClaims(claims, clientGroupId, clientId);
        claims.setClaim(SUBJECT, "client-user:" + clientId);
        claims.setClaim(EXTRA_CLAIM_CLIENT_USER_ID, UUID.randomUUID());
        claims.setClaim(EXTRA_CLAIM_USER_ID, userId.toString());
        return claims;
    }

    private static JwtClaims createClaims(String issueForService) {
        JwtClaims claims = new JwtClaims();
        claims.setClaim(EXTRA_CLAIM_ISSUED_FOR, issueForService);
        claims.setIssuedAt(NumericDate.now());
        NumericDate expiration = NumericDate.now();
        expiration.addSeconds(360); // Good enough for testing
        claims.setExpirationTime(expiration);
        claims.setJwtId(UUID.randomUUID().toString());

        return claims;
    }

    private static void addClientClaims(JwtClaims claims, UUID clientGroupId, UUID clientId) {
        claims.setClaim(EXTRA_CLAIM_CLIENT_ID, clientId.toString());
        claims.setClaim(EXTRA_CLAIM_CLIENT_GROUP_ID, clientGroupId.toString());
        claims.setClaim(EXTRA_CLAIM_CLIENT_NAME, clientId + "-client-name");
        claims.setClaim(EXTRA_CLAIM_CLIENT_GROUP_NAME, clientGroupId + "-client-group-name");
        claims.setClaim(CLAIM_PSD2_LICENSED, true);
        claims.setClaim(CLAIM_AIS, true);
        claims.setClaim(CLAIM_PIS, true);
        claims.setClaim(CLAIM_CAM, true);
        claims.setClaim(CLAIM_CLIENT_USERS_KYC_PRIVATE_INDIVIDUALS, false);
        claims.setClaim(CLAIM_CLIENT_USERS_KYC_ENTITIES, false);
        claims.setClaim(CLAIM_DATA_ENRICHMENT_CATEGORIZATION, true);
        claims.setClaim(CLAIM_DATA_ENRICHMENT_MERCHANT_RECOGNITION, true);
        claims.setClaim(CLAIM_DATA_ENRICHMENT_CYCLE_DETECTION, true);
        claims.setClaim(CLAIM_DATA_ENRICHMENT_LABELS, true);
        claims.setClaim(CLAIM_DELETED, false);
    }
}
