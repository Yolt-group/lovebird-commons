package nl.ing.lovebird.clienttokens.verification;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import nl.ing.lovebird.clienttokens.AbstractClientToken;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.clienttokens.TestTokenCreator;
import nl.ing.lovebird.clienttokens.constants.ClientTokenConstants;
import nl.ing.lovebird.clienttokens.verification.exception.InvalidClientTokenException;
import nl.ing.lovebird.logging.SemaEventLogger;
import nl.ing.lovebird.logging.test.CaptureLogEvents;
import nl.ing.lovebird.logging.test.LogEvents;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.ReservedClaimNames;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.InvalidJwtSignatureException;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UnresolvableKeyException;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.Security;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@CaptureLogEvents
class ClientTokenParserTest {

    private static final String SIGNATURE_KID = UUID.randomUUID().toString();
    private static final PrivateKey SIGNATURE_PRIVATE_KEY;
    private static final String JSON_WEB_KEY_SET;
    private static final ClientTokenParser SUBJECT;

    static {
        Security.addProvider(new BouncyCastleProvider());
        try {
            RsaJsonWebKey jsonWebKey = TestTokenCreator.generateRsaJsonWebKey(SIGNATURE_KID);
            SIGNATURE_PRIVATE_KEY = jsonWebKey.getRsaPrivateKey();
            JSON_WEB_KEY_SET = new JsonWebKeySet(jsonWebKey).toJson();
            SUBJECT = new ClientTokenParser(JSON_WEB_KEY_SET);
        } catch (JoseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void throwsExceptionDuringConstructionWhenJwksHasAPrivateComponent() {
        String jwksJson = "{\n" +
                "  \"keys\": [\n" +
                "    {\n" +
                "      \"kty\": \"RSA\",\n" +
                "      \"d\": \"WCGVnA3drq13a6tEROzWalg9Lxs8JJIVY5xGBBN-SOVLVqeh18ZSUHWbYa0rtNmJT3NCHR6ILexCbVenipty8J6mi7r_CPPeL2P68C60AePiHPhBt67WV6EUz-rV9XV8tDJ0J9jkyE9JF9E5SAENXtznN3C6REp2bP5xQjSL8Fw5vZax2idntYwhcxnXs9C3RcOsj_Qw0ckbLY2AAEyUydN2gP8tBshUsWlTZEJJKonOeyhVhVSvjbQmPBeLIwJnV6PE7ZGy-aQXFgXPK18RKZWeW9UHEWOcVz1JE2PCB_hrqIXI7o5ybXRF_pqj7kbDYUMhvNKxmB2VkiSEyAaTYQ\",\n" +
                "      \"e\": \"AQAB\",\n" +
                "      \"use\": \"sig\",\n" +
                "      \"kid\": \"0d257f29-dbbf-4fa3-a602-6eb0d498be0e\",\n" +
                "      \"alg\": \"RS256\",\n" +
                "      \"n\": \"u5ORko1Zj7CA-U4EQh70jagipYx_btvVnQu4K9fY3JRar-ETnaHFhuIaIf3ZktU34JQQ5ZnAHsPPhuazMufWEMYg8VBeNkwDwG8GnoJDUFgX83L8eXQLfl2eGrwCIXvLZ2rRukWs3xxpaYFfYvzJIL4BXcec8M7wCGu7od52kCMN9Axq-LmS_g3lD_JHezFogbMigD0N1dx75OxhCr4kpSEx2BUt3PX9txg_ucTlZ-wTriFKfHeFxJXYyN49aE5gkZzRDjdLNnXCpxrU4-oj7V5tHyEHfDJMYltYQcee-YAP4bLRCkB-OQ5hijGkg-ay3aAvk-O1vZ0N46BHRkiBxQ\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        assertThatThrownBy(() -> new ClientTokenParser(jwksJson))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Detected a private component (\"d\") in the signature jwks");
    }

    @Test
    void throwsExceptionDuringConstructionWhenJwksIsMissing() {
        assertThatThrownBy(() -> new ClientTokenParser((String) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("signatureJWKSJson");
    }

    @Test
    void throwsExceptionWhenClientTokenIsNotAJWT() {
        assertThatThrownBy(() -> SUBJECT.parseClientToken("abcd"))
                .isInstanceOf(InvalidClientTokenException.class)
                .hasMessageContaining("doesn't match the jwt pattern");
    }

    @Test
    void throwsExceptionWhenClientTokenIsSignedByUnknownPrivateKey() throws JoseException {
        PrivateKey unsupportedSignaturePrivateKey =
                TestTokenCreator.generateRsaJsonWebKey(SIGNATURE_KID).getRsaPrivateKey();
        String clientToken = TestTokenCreator.createJsonWebSignature(
                TestTokenCreator.createJwtClaims(), unsupportedSignaturePrivateKey, SIGNATURE_KID
        ).getCompactSerialization();
        assertThatThrownBy(() -> SUBJECT.parseClientToken(clientToken))
                .isInstanceOf(InvalidClientTokenException.class)
                .hasCauseInstanceOf(InvalidJwtSignatureException.class);
    }

    @Test
    void throwsExceptionWhenClientTokenHasAnUnknownKeyId() throws JoseException {
        String clientToken = TestTokenCreator.createJsonWebSignature(
                TestTokenCreator.createJwtClaims(), SIGNATURE_PRIVATE_KEY, "unknown-kid"
        ).getCompactSerialization();
        assertThatThrownBy(() -> SUBJECT.parseClientToken(clientToken))
                .isInstanceOf(InvalidClientTokenException.class)
                .hasRootCauseInstanceOf(UnresolvableKeyException.class);
    }

    @Test
    void throwsExceptionWhenClientTokenIsMissingSubjectClaim() throws JoseException {
        JwtClaims claims = TestTokenCreator.createJwtClaims();
        claims.unsetClaim(ReservedClaimNames.SUBJECT);
        String clientToken = TestTokenCreator.createJsonWebSignature(
                claims, SIGNATURE_PRIVATE_KEY, SIGNATURE_KID
        ).getCompactSerialization();
        assertThatThrownBy(() -> SUBJECT.parseClientToken(clientToken))
                .isInstanceOf(InvalidClientTokenException.class)
                .hasCauseInstanceOf(InvalidJwtException.class)
                .hasMessageContaining("No Subject (sub) claim is present");
    }

    @Test
    void throwsExceptionWhenClientTokenIsMissingExpirationTimeClaim() throws JoseException {
        JwtClaims claims = TestTokenCreator.createJwtClaims();
        claims.unsetClaim(ReservedClaimNames.EXPIRATION_TIME);
        String clientToken = TestTokenCreator.createJsonWebSignature(
                claims, SIGNATURE_PRIVATE_KEY, SIGNATURE_KID
        ).getCompactSerialization();
        assertThatThrownBy(() -> SUBJECT.parseClientToken(clientToken))
                .isInstanceOf(InvalidClientTokenException.class)
                .hasCauseInstanceOf(InvalidJwtException.class)
                .hasMessageContaining("No Expiration Time (exp) claim present");
    }

    @Test
    void logsSemaWhenClientUserTokenIsExpired(LogEvents events) throws Exception {
        JwtClaims claims = TestTokenCreator.createJwtClaims();
        claims.setClaim(ClientTokenConstants.EXTRA_CLAIM_USER_ID, UUID.randomUUID());
        claims.setClaim(ClientTokenConstants.EXTRA_CLAIM_CLIENT_USER_ID, UUID.randomUUID());
        NumericDate expirationDate = NumericDate.now();
        expirationDate.addSeconds(-5);
        claims.setExpirationTime(expirationDate);
        String clientToken = TestTokenCreator.createJsonWebSignature(
                claims, SIGNATURE_PRIVATE_KEY, SIGNATURE_KID
        ).getCompactSerialization();

        SUBJECT.parseClientToken(clientToken);

        ILoggingEvent event = events.stream(SemaEventLogger.class, Level.INFO).findFirst().get();
        assertEquals(event.getFormattedMessage(),
                String.format("client-token expired: issued-at: %s; expired-at: %s; issued-for: %s; user-id: %s; client-user-id: %s; client-id: %s; client-group-id: %s;",
                        claims.getIssuedAt().getValue(),
                        claims.getExpirationTime().getValue(),
                        claims.getClaimValue(ClientTokenConstants.EXTRA_CLAIM_ISSUED_FOR),
                        claims.getClaimValue(ClientTokenConstants.EXTRA_CLAIM_USER_ID),
                        claims.getClaimValue(ClientTokenConstants.EXTRA_CLAIM_CLIENT_USER_ID),
                        claims.getClaimValue(ClientTokenConstants.EXTRA_CLAIM_CLIENT_ID),
                        claims.getClaimValue(ClientTokenConstants.EXTRA_CLAIM_CLIENT_GROUP_ID)));
    }

    @Test
    void logsSemaWhenClientTokenIsExpired(LogEvents events) throws Exception {
        JwtClaims claims = TestTokenCreator.createJwtClaims();
        NumericDate expirationDate = NumericDate.now();
        expirationDate.addSeconds(-5);
        claims.setExpirationTime(expirationDate);
        String clientToken = TestTokenCreator.createJsonWebSignature(
                claims, SIGNATURE_PRIVATE_KEY, SIGNATURE_KID
        ).getCompactSerialization();

        SUBJECT.parseClientToken(clientToken);

        ILoggingEvent event = events.stream(SemaEventLogger.class, Level.INFO).findFirst().get();
        assertEquals(event.getFormattedMessage(),
                String.format("client-token expired: issued-at: %s; expired-at: %s; issued-for: %s; user-id: %s; client-user-id: %s; client-id: %s; client-group-id: %s;",
                        claims.getIssuedAt().getValue(),
                        claims.getExpirationTime().getValue(),
                        claims.getClaimValue(ClientTokenConstants.EXTRA_CLAIM_ISSUED_FOR),
                        null,
                        null,
                        claims.getClaimValue(ClientTokenConstants.EXTRA_CLAIM_CLIENT_ID),
                        claims.getClaimValue(ClientTokenConstants.EXTRA_CLAIM_CLIENT_GROUP_ID)));
    }

    @Test
    void throwsExceptionWhenClientTokenIsMissingIsfClaim() throws JoseException {
        JwtClaims claims = TestTokenCreator.createJwtClaims();
        claims.unsetClaim(ClientTokenConstants.EXTRA_CLAIM_ISSUED_FOR);
        String clientToken = TestTokenCreator.createJsonWebSignature(
                claims, SIGNATURE_PRIVATE_KEY, SIGNATURE_KID
        ).getCompactSerialization();
        assertThatThrownBy(() -> SUBJECT.parseClientToken(clientToken))
                .isInstanceOf(InvalidClientTokenException.class)
                .hasCauseInstanceOf(InvalidJwtException.class)
                .hasMessageContaining("missing claim isf");
    }

    @Test
    void throwsExceptionWhenClientTokenIsMissingClientGroupIdClaim() throws JoseException {
        JwtClaims claims = TestTokenCreator.createJwtClaims();
        claims.unsetClaim(ClientTokenConstants.EXTRA_CLAIM_CLIENT_GROUP_ID);
        String clientToken = TestTokenCreator.createJsonWebSignature(
                claims, SIGNATURE_PRIVATE_KEY, SIGNATURE_KID
        ).getCompactSerialization();
        assertThatThrownBy(() -> SUBJECT.parseClientToken(clientToken))
                .isInstanceOf(InvalidClientTokenException.class)
                .hasCauseInstanceOf(InvalidJwtException.class)
                .hasMessageContaining("missing claim client-group-id");
    }

    @Test
    void throwsExceptionWhenClientTokenHasEmptyIsfClaim() throws JoseException {
        JwtClaims claims = TestTokenCreator.createJwtClaims();
        claims.setClaim(ClientTokenConstants.EXTRA_CLAIM_ISSUED_FOR, "");
        String clientToken = TestTokenCreator.createJsonWebSignature(
                claims, SIGNATURE_PRIVATE_KEY, SIGNATURE_KID
        ).getCompactSerialization();
        assertThatThrownBy(() -> SUBJECT.parseClientToken(clientToken))
                .isInstanceOf(InvalidClientTokenException.class)
                .hasCauseInstanceOf(InvalidJwtException.class)
                .hasMessageContaining("empty or null claim");
    }

    @Test
    void parsesClientTokenConfigurationCorrectly() throws JoseException {
        JwtClaims claims = TestTokenCreator.createJwtClaims();
        claims.setClaim(ClientTokenConstants.CLAIM_CLIENT_USERS_KYC_PRIVATE_INDIVIDUALS, true);
        claims.setClaim(ClientTokenConstants.CLAIM_CLIENT_USERS_KYC_ENTITIES, true);
        claims.setClaim(ClientTokenConstants.CLAIM_PSD2_LICENSED, false);
        claims.setClaim(ClientTokenConstants.CLAIM_AIS, false);
        claims.setClaim(ClientTokenConstants.CLAIM_PIS, true);
        claims.setClaim(ClientTokenConstants.CLAIM_CAM, true);
        claims.setClaim(ClientTokenConstants.CLAIM_DATA_ENRICHMENT_MERCHANT_RECOGNITION, true);
        claims.setClaim(ClientTokenConstants.CLAIM_DATA_ENRICHMENT_CATEGORIZATION, true);
        claims.setClaim(ClientTokenConstants.CLAIM_DATA_ENRICHMENT_CYCLE_DETECTION, false);
        claims.setClaim(ClientTokenConstants.CLAIM_DATA_ENRICHMENT_LABELS, false);
        claims.setClaim(ClientTokenConstants.CLAIM_DELETED, true);
        claims.setClaim(ClientTokenConstants.CLAIM_ONE_OFF_AIS, true);
        claims.setClaim(ClientTokenConstants.CLAIM_CONSENT_STARTER, true);
        claims.setClaim(ClientTokenConstants.CLAIM_RISK_INSIGHTS, true);

        String clientTokenSerialized = TestTokenCreator.createJsonWebSignature(
                claims, SIGNATURE_PRIVATE_KEY, SIGNATURE_KID
        ).getCompactSerialization();

        ClientToken clientToken = (ClientToken) SUBJECT.parseClientToken(clientTokenSerialized);
        assertTrue(clientToken.hasKYCForPrivateIndividuals());
        assertTrue(clientToken.hasKYCForEntities());
        assertFalse(clientToken.isPSD2Licensed());
        assertFalse(clientToken.hasAIS());
        assertTrue(clientToken.hasPIS());
        assertTrue(clientToken.hasCAM());
        assertTrue(clientToken.hasDataEnrichmentMerchantRecognition());
        assertTrue(clientToken.hasDataEnrichmentCategorization());
        assertFalse(clientToken.hasDataEnrichmentCycleDetection());
        assertFalse(clientToken.hasDataEnrichmentLabels());
        assertTrue(clientToken.isDeleted());
        assertTrue(clientToken.hasOneOffAIS());
        assertTrue(clientToken.hasConsentStarter());
        assertTrue(clientToken.hasRiskInsights());
    }

    @Test
    void parsesClientUserTokenConfigurationCorrectly() throws JoseException {
        JwtClaims claims = TestTokenCreator.createJwtClaims();
        claims.setSubject("client-user: " + UUID.randomUUID());
        UUID clientUserId = UUID.randomUUID();
        claims.setClaim(ClientTokenConstants.EXTRA_CLAIM_CLIENT_USER_ID, clientUserId);
        claims.setClaim(ClientTokenConstants.EXTRA_CLAIM_USER_ID, UUID.randomUUID());

        String clientTokenSerialized = TestTokenCreator.createJsonWebSignature(
                claims, SIGNATURE_PRIVATE_KEY, SIGNATURE_KID
        ).getCompactSerialization();

        ClientUserToken clientToken = (ClientUserToken) SUBJECT.parseClientToken(clientTokenSerialized);
        assertEquals(clientUserId, clientToken.getClientUserIdClaim());
    }

    @Test
    void throwsIllegalStateExceptionWhenPSD2ClaimIsMissing() throws JoseException {
        ClientToken clientToken = (ClientToken) createTokenWithoutClaims();
        assertThatThrownBy(clientToken::isPSD2Licensed)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("The claim psd2-licensed was not found in the client-token");
    }

    @Test
    void throwsIllegalStateExceptionWhenKYCForPrivateIndividualsIsMissing() throws JoseException {
        ClientToken clientToken = (ClientToken) createTokenWithoutClaims();
        assertThatThrownBy(clientToken::hasKYCForPrivateIndividuals)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("The claim client-users-kyc-private-individuals was not found in the client-token");
    }

    @Test
    void throwsIllegalStateExceptionWhenKYCForEntitiesIsMissing() throws JoseException {
        ClientToken clientToken = (ClientToken) createTokenWithoutClaims();
        assertThatThrownBy(clientToken::hasKYCForEntities)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("The claim client-users-kyc-entities was not found in the client-token");
    }

    private AbstractClientToken createTokenWithoutClaims() throws JoseException {
        JwtClaims claims = TestTokenCreator.createJwtClaims();
        String clientTokenSerialized = TestTokenCreator.createJsonWebSignature(
                claims, SIGNATURE_PRIVATE_KEY, SIGNATURE_KID
        ).getCompactSerialization();

        return SUBJECT.parseClientToken(clientTokenSerialized);
    }
}
