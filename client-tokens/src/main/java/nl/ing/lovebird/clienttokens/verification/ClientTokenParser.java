package nl.ing.lovebird.clienttokens.verification;

import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.AbstractClientToken;
import nl.ing.lovebird.clienttokens.ClientGroupToken;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.clienttokens.constants.ClientTokenConstants;
import nl.ing.lovebird.clienttokens.verification.exception.InvalidClientTokenException;
import nl.ing.lovebird.clienttokens.verification.sema.ClientTokenExpiredSemaEvent;
import nl.ing.lovebird.clienttokens.verification.sema.PrivateComponentInPublicJWKSSemaEvent;
import nl.ing.lovebird.logging.SemaEventLogger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.VerificationJwkSelector;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.ErrorCodeValidator;
import org.jose4j.jwt.consumer.ErrorCodes;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.keys.resolvers.VerificationKeyResolver;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UnresolvableKeyException;
import org.springframework.util.Assert;

import java.security.Key;
import java.security.Security;
import java.text.MessageFormat;
import java.util.List;

/**
 * Parses and verifies a client-token.
 */
public class ClientTokenParser {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final JwtConsumer jwtConsumer;

    public ClientTokenParser(String signatureJWKSJson) {
        this(new JwksVerificationKeyResolver(signatureJWKSJson));
    }

    public ClientTokenParser(VerificationKeyResolver verificationKeyResolver) {
        this.jwtConsumer = new JwtConsumerBuilder()
                .setRequireSubject()
                .setRequireExpirationTime()
                .registerValidator(ClientTokenParser::hasRequiredClaims)
                .setVerificationKeyResolver(verificationKeyResolver)
                .setJwsAlgorithmConstraints(new AlgorithmConstraints(
                        AlgorithmConstraints.ConstraintType.PERMIT,
                        AlgorithmIdentifiers.RSA_PSS_USING_SHA512
                ))
                .build();
    }

    /**
     * Parses and verifies the provided clientToken.
     * @param clientToken A serialized client-token (formatted as JWS)
     * @return The parsed clientToken
     * @throws IllegalArgumentException when clientToken is null
     * @throws InvalidClientTokenException when clientToken cannot be parsed, or the token is invalid
     */
    public AbstractClientToken parseClientToken(String clientToken) {
        Assert.notNull(clientToken, "clientToken may not be null");
        if (!clientToken.matches(ClientTokenConstants.JWT_REGEX)) {
            throw new InvalidClientTokenException("client-token doesn't match the jwt pattern");
        }
        AbstractClientToken result;
        try {
            JwtClaims claims = this.jwtConsumer.processToClaims(clientToken);
            if (containsUserId(claims)) {
                result = new ClientUserToken(clientToken, claims);
            } else if (containsClientId(claims)) {
                result = new ClientToken(clientToken, claims);
            } else {
                result = new ClientGroupToken(clientToken, claims);
            }
        } catch (InvalidJwtException e) {
            List<ErrorCodeValidator.Error> errorDetails = e.getErrorDetails();
            if (errorDetails.size() == 1 && errorDetails.get(0).getErrorCode() == ErrorCodes.EXPIRED) {
                JwtClaims claims = e.getJwtContext().getJwtClaims();
                if (containsUserId(claims)) {
                    ClientUserToken token = new ClientUserToken(clientToken, claims);
                    SemaEventLogger.log(new ClientTokenExpiredSemaEvent(token.getIssuedAt(),
                            token.getExpirationTime(),
                            token.getIssuedForClaim(),
                            token.getClientGroupIdClaim(),
                            token.getClientIdClaim(),
                            token.getClientUserIdClaim(),
                            token.getUserIdClaim()));
                    result = token;
                } else if (containsClientId(claims)) {
                    ClientToken token = new ClientToken(clientToken, claims);
                    SemaEventLogger.log(new ClientTokenExpiredSemaEvent(token.getIssuedAt(),
                            token.getExpirationTime(),
                            token.getIssuedForClaim(),
                            token.getClientGroupIdClaim(),
                            token.getClientIdClaim(),
                            null, null));
                    result = token;
                } else {
                    ClientGroupToken token = new ClientGroupToken(clientToken, claims);
                    SemaEventLogger.log(new ClientTokenExpiredSemaEvent(token.getIssuedAt(),
                            token.getExpirationTime(),
                            token.getIssuedForClaim(),
                            token.getClientGroupIdClaim(),
                            null, null, null));
                    result = token;
                }
            } else {
                throw new InvalidClientTokenException(e);
            }
        }
        return result;
    }

    private boolean containsUserId(JwtClaims claims) {
        return claims.hasClaim(ClientTokenConstants.EXTRA_CLAIM_USER_ID);
    }

    private boolean containsClientId(JwtClaims claims) {
        return claims.hasClaim(ClientTokenConstants.EXTRA_CLAIM_CLIENT_ID);
    }

    private static String hasRequiredClaims(JwtContext jwtContext) {
        for (String name : ClientTokenConstants.CLIENT_TOKEN_REQUIRED_CLAIMS) {
            if (!jwtContext.getJwtClaims().hasClaim(name)) {
                return MessageFormat.format("Invalid client-token: missing claim {0}", name);
            }

            try {
                String claimValue = jwtContext.getJwtClaims().getStringClaimValue(name);
                if (claimValue == null || claimValue.isEmpty()) {
                    return MessageFormat.format("Invalid client-token: empty or null claim {0}", name);
                }
            } catch (MalformedClaimException e) {
                return MessageFormat.format("Invalid client-token: claim not parsable to String {0}", name);
            }
        }
        return null;
    }

    /**
     * Selects the key that was used to sign the JWS using a standard jose4j implementation.
     */
    @Slf4j
    private static class JwksVerificationKeyResolver implements VerificationKeyResolver {

        private final VerificationJwkSelector jwkSelector = new VerificationJwkSelector();
        private final JsonWebKeySet signatureJWKS;

        private JwksVerificationKeyResolver(String signatureJWKSJson) {
            this.signatureJWKS = this.buildSignatureJWKS(signatureJWKSJson);
        }

        private JsonWebKeySet buildSignatureJWKS(String signatureJWKSJson) {
            if (signatureJWKSJson == null) {
                throw new IllegalArgumentException("signatureJWKSJson is null");
            }

            disallowPrivateComponent(signatureJWKSJson);

            try {
                JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(signatureJWKSJson);
                if (jsonWebKeySet.getJsonWebKeys().isEmpty()) {
                    throw new IllegalStateException("No keys found for verifying the signature of the client token");
                }
                return jsonWebKeySet;
            } catch (JoseException e) {
                throw new IllegalStateException("Failed parsing the signature JWKS into a JsonWebTokenSet", e);
            }
        }

        private void disallowPrivateComponent(String signatureJWKSJson) {
            // RSA JWKs contain RSA parameters: https://www.gnupg.org/documentation/manuals/gcrypt-devel/RSA-key-parameters.html.
            // To be certain that we do not use compromised keys, let's verify that the private component is missing.
            if (signatureJWKSJson.contains("\"d\"")) {
                String message = "Detected a private component (\"d\") in the signature jwks, this means this entry is compromised, please remove it. Ignoring all JWKS from this set.";
                SemaEventLogger.log(new PrivateComponentInPublicJWKSSemaEvent(message));
                throw new IllegalStateException(message);
            }
        }

        @Override
        public Key resolveKey(JsonWebSignature jws, List<JsonWebStructure> nestingContext)
                throws UnresolvableKeyException {
            try {
                JsonWebKey jsonWebKey = jwkSelector.select(jws, signatureJWKS.getJsonWebKeys());
                if (jsonWebKey == null) {
                    throw new UnresolvableKeyException(String.format(
                            "Cannot find the right verification key for the following jws (headers): %s. Number of candidates: %s",
                            jws.getHeaders().getFullHeaderAsJsonString(),
                            signatureJWKS.getJsonWebKeys().size()
                    ));
                }
                return jsonWebKey.getKey();
            } catch (JoseException e) {
                throw new UnresolvableKeyException("Something failed when selecting the verification keys from the jwk candidates", e);
            }
        }
    }
}
