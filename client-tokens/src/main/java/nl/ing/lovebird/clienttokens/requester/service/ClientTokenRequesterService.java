package nl.ing.lovebird.clienttokens.requester.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.AbstractClientToken;
import nl.ing.lovebird.clienttokens.ClientGroupToken;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.clienttokens.verification.ClientTokenParser;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.lang.JoseException;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ClientTokenRequesterService {

    private static final int REQUEST_TOKEN_EXPIRATION_TIME = 10;
    private static final String SIGNATURE_ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA512;
    private static final Clock DEFAULT_CLOCK = Clock.systemUTC();

    private final TokensRestClient tokensRestClient;
    private final ClientTokenParser parser;
    private final RsaJsonWebKey signingKey;
    private final String requester;

    private final Cache<ClientUserCacheKey, ClientUserToken> CLIENT_USER_CACHE = Caffeine
            .newBuilder()
            .maximumSize(1_000_000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    private final Cache<UUID, ClientToken> CLIENT_CACHE = Caffeine
            .newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    private final Cache<UUID, ClientGroupToken> CLIENT_GROUP_CACHE = Caffeine
            .newBuilder()
            .maximumSize(100)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    private final Clock clock;

    public ClientTokenRequesterService(
            TokensRestClient tokensRestClient,
            RsaJsonWebKey signingKeyJwk,
            String requester,
            ClientTokenParser parser
    ) {
        this(tokensRestClient, signingKeyJwk, requester, parser, DEFAULT_CLOCK);
    }

    /**
     * VisibleForTesting
     */
    ClientTokenRequesterService(
            TokensRestClient tokensRestClient,
            RsaJsonWebKey signingKeyJwk,
            String requester,
            ClientTokenParser parser,
            Clock clock
    ) {
        this.tokensRestClient = tokensRestClient;
        this.requester = requester;
        this.parser = parser;
        this.clock = clock;
        signingKey = signingKeyJwk;
        if (signingKey.getRsaPrivateKey() == null) {
            throw new IllegalStateException("Expected a private key in the signing JWK for requester: " + requester);
        }
    }

    /**
     * Retrieve a client-token for the provided client-id and user-id.
     *
     * @param clientId The client-id to retrieve a client-token for
     * @param userId   The user-id to retrieve a client-token for
     * @return A validated, typed client-token
     */
    public ClientUserToken getClientUserToken(UUID clientId, UUID userId) {
        return getCachedToken(new ClientUserCacheKey(clientId, userId), CLIENT_USER_CACHE, "client-user:" + clientId + "," + userId);
    }

    /**
     * Retrieve a client-token for the provided client-id.
     *
     * @param clientId The client-id to retrieve a client-token for
     * @return A validated, typed client-token
     */
    public ClientToken getClientToken(UUID clientId) {
        return getCachedToken(clientId, CLIENT_CACHE, "client:" + clientId.toString());
    }

    /**
     * Retrieve a client-token for the provided client-group-id.
     *
     * @param clientGroupId The client-group-id to retrieve a client-token for
     * @return A validated, typed client-token
     */
    public ClientGroupToken getClientGroupToken(UUID clientGroupId) {
        return getCachedToken(clientGroupId, CLIENT_GROUP_CACHE, "group:" + clientGroupId.toString());
    }

    private <T extends AbstractClientToken, U> T getCachedToken(U id, Cache<U, T> tokenCache, String subject) {
        return tokenCache.get(id, key -> fetchClientToken(subject));
    }

    private <T extends AbstractClientToken> T fetchClientToken(String subject) {
        ClientTokenResponseDTO clientTokenResponse = requestToken(subject);
        T verifiedClientToken = parseToken(clientTokenResponse);

        Instant now = Instant.now(clock);
        log.info("Received a new client token for service: {} and {} with expiry time: {}",
                requester, subject, now.plusSeconds(clientTokenResponse.getExpiresIn()).toString());

        return verifiedClientToken;
    }

    private ClientTokenResponseDTO requestToken(String subject) {
        log.info("Requesting new client-token for service: {} and {}", requester, subject);
        String requestToken;
        try {
            requestToken = generateRequestToken(subject);
        } catch (JoseException e) {
            throw new ClientTokenRequestTokenException("Failed creating a request token for getting a client token", e);
        }
        return tokensRestClient.getClientToken(requestToken);
    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractClientToken> T parseToken(ClientTokenResponseDTO clientTokenResponse) {
        // Process token to typed (and validated) ClientToken
        String serializedClientToken = clientTokenResponse.getClientToken();
        return (T) parser.parseClientToken(serializedClientToken);
    }

    private String generateRequestToken(String subject) throws JoseException {
        JwtClaims claims = new JwtClaims();
        claims.setIssuer(requester);
        claims.setSubject(subject);
        claims.setJwtId(UUID.randomUUID().toString());
        claims.setIssuedAt(NumericDate.now());
        claims.setExpirationTime(determineExpirationDate());

        JsonWebSignature requestToken = new JsonWebSignature();
        requestToken.setPayload(claims.toJson());
        requestToken.setKey(signingKey.getRsaPrivateKey());
        requestToken.setKeyIdHeaderValue(signingKey.getKeyId());
        requestToken.setAlgorithmHeaderValue(SIGNATURE_ALGORITHM);

        return requestToken.getCompactSerialization();
    }

    private NumericDate determineExpirationDate() {
        NumericDate expirationDate = NumericDate.now();
        expirationDate.addSeconds(REQUEST_TOKEN_EXPIRATION_TIME);
        // Add 5 more seconds since we also communicate the expiration-in-seconds as part of the a response (oauth 2 spec).
        // The client should be able to rely on that, so the token actually expires a bit later.
        // For example, of expiration-in-seconds = 60. The token should still be valid for 60 seconds the moment the client
        // sees this response. Therefore, the actual token contains a value that is valid for a bit longer to adjust for
        // network latencies etc.
        expirationDate.addSeconds(5L);
        return expirationDate;
    }

    @ToString
    @EqualsAndHashCode
    @RequiredArgsConstructor
    private static class ClientUserCacheKey {
        @NonNull
        public final UUID clientId;
        @NonNull
        public final UUID userId;
    }
}
