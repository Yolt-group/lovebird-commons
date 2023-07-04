package nl.ing.lovebird.clienttokens.requester.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import nl.ing.lovebird.clienttokens.ClientGroupToken;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.clienttokens.verification.ClientTokenParser;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.jwx.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.PublicKey;
import java.security.Security;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientTokenRequesterServiceTest {

    private static final String KEY_ID = UUID.randomUUID().toString();
    private static final String CLIENT_TOKEN_REQUESTER = "site-management";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID CLIENT_ID = UUID.randomUUID();
    private static final UUID CLIENT_GROUP_ID = UUID.randomUUID();

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Mock
    private TokensRestClient tokensRestClient;
    @Mock
    private ClientTokenParser parser;

    private RsaJsonWebKey signingKeyJwk;
    private PublicKey signatureVerificationKey;

    private ClientTokenRequesterService clientTokenService;

    @BeforeEach
    void before() throws Exception {
        RsaJsonWebKey rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
        rsaJsonWebKey.setKeyId(KEY_ID);
        signingKeyJwk = rsaJsonWebKey;
        signatureVerificationKey = rsaJsonWebKey.getPublicKey();
        clientTokenService = new ClientTokenRequesterService(tokensRestClient, signingKeyJwk, CLIENT_TOKEN_REQUESTER, parser);
    }

    @Test
    void requestClientUserToken() throws Exception {
        ArgumentCaptor<String> requestTokenCaptor = ArgumentCaptor.forClass(String.class);
        String expectedClientUserToken = "client-token";
        ClientTokenResponseDTO clientTokenResponse = new ClientTokenResponseDTO(expectedClientUserToken, 60);
        when(tokensRestClient.getClientToken(requestTokenCaptor.capture())).thenReturn(clientTokenResponse);
        when(parser.parseClientToken(expectedClientUserToken)).thenReturn(new ClientUserToken(expectedClientUserToken, null));

        ClientUserToken actualClientToken = clientTokenService.getClientUserToken(CLIENT_ID, USER_ID);

        assertEquals(expectedClientUserToken, actualClientToken.getSerialized());
        String requestToken = requestTokenCaptor.getValue();
        JwtConsumer jwtConsumer = new JwtConsumerBuilder().setVerificationKey(signatureVerificationKey).build();
        JwtContext jwtContext = jwtConsumer.process(requestToken);

        Headers headers = jwtContext.getJoseObjects().get(0).getHeaders();
        assertEquals(KEY_ID, headers.getStringHeaderValue("kid"));
        assertEquals("PS512", headers.getStringHeaderValue("alg"));
        JwtClaims claims = jwtContext.getJwtClaims();
        assertEquals(CLIENT_TOKEN_REQUESTER, claims.getIssuer());
        assertEquals("client-user:" + CLIENT_ID + "," + USER_ID, claims.getSubject());
        assertNotNull(claims.getJwtId());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpirationTime());
    }

    @Test
    void requestClientToken() throws Exception {
        ArgumentCaptor<String> requestTokenCaptor = ArgumentCaptor.forClass(String.class);
        String expectedClientToken = "client-token";
        ClientTokenResponseDTO clientTokenResponse = new ClientTokenResponseDTO(expectedClientToken, 60);
        when(tokensRestClient.getClientToken(requestTokenCaptor.capture())).thenReturn(clientTokenResponse);
        when(parser.parseClientToken(expectedClientToken)).thenReturn(new ClientToken(expectedClientToken, null));

        ClientToken actualClientToken = clientTokenService.getClientToken(CLIENT_ID);

        assertEquals(expectedClientToken, actualClientToken.getSerialized());
        String requestToken = requestTokenCaptor.getValue();
        JwtConsumer jwtConsumer = new JwtConsumerBuilder().setVerificationKey(signatureVerificationKey).build();
        JwtContext jwtContext = jwtConsumer.process(requestToken);

        Headers headers = jwtContext.getJoseObjects().get(0).getHeaders();
        assertEquals(KEY_ID, headers.getStringHeaderValue("kid"));
        assertEquals("PS512", headers.getStringHeaderValue("alg"));
        JwtClaims claims = jwtContext.getJwtClaims();
        assertEquals(CLIENT_TOKEN_REQUESTER, claims.getIssuer());
        assertEquals("client:" + CLIENT_ID.toString(), claims.getSubject());
        assertNotNull(claims.getJwtId());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpirationTime());
    }

    @Test
    void requestClientGroupToken() throws Exception {
        ArgumentCaptor<String> requestTokenCaptor = ArgumentCaptor.forClass(String.class);
        String expectedToken = "client-group-token";
        ClientTokenResponseDTO clientTokenResponse = new ClientTokenResponseDTO(expectedToken, 60);
        when(tokensRestClient.getClientToken(requestTokenCaptor.capture())).thenReturn(clientTokenResponse);
        when(parser.parseClientToken(expectedToken)).thenReturn(new ClientGroupToken(expectedToken, null));

        ClientGroupToken actualToken = clientTokenService.getClientGroupToken(CLIENT_GROUP_ID);

        assertEquals(expectedToken, actualToken.getSerialized());
        String requestToken = requestTokenCaptor.getValue();
        JwtConsumer jwtConsumer = new JwtConsumerBuilder().setVerificationKey(signatureVerificationKey).build();
        JwtContext jwtContext = jwtConsumer.process(requestToken);

        Headers headers = jwtContext.getJoseObjects().get(0).getHeaders();
        assertEquals(KEY_ID, headers.getStringHeaderValue("kid"));
        assertEquals("PS512", headers.getStringHeaderValue("alg"));
        JwtClaims claims = jwtContext.getJwtClaims();
        assertEquals(CLIENT_TOKEN_REQUESTER, claims.getIssuer());
        assertEquals("group:" + CLIENT_GROUP_ID.toString(), claims.getSubject());
        assertNotNull(claims.getJwtId());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpirationTime());
    }

    @Test
    void cachingClientTokensWorks() {
        stubGetClientToken(60);

        clientTokenService.getClientToken(CLIENT_ID);
        clientTokenService.getClientToken(CLIENT_ID);
        verify(tokensRestClient, times(1)).getClientToken(anyString());

        UUID otherClientId = UUID.randomUUID();
        clientTokenService.getClientToken(otherClientId);
        verify(tokensRestClient, times(2)).getClientToken(anyString());
    }
    @Test
    void cachingClientGroupTokensWorks() {
        stubGetClientGroupToken(60);

        clientTokenService.getClientGroupToken(CLIENT_GROUP_ID);
        clientTokenService.getClientGroupToken(CLIENT_GROUP_ID);
        verify(tokensRestClient, times(1)).getClientToken(anyString());

        UUID otherClientGroupId = UUID.randomUUID();
        clientTokenService.getClientGroupToken(otherClientGroupId);
        verify(tokensRestClient, times(2)).getClientToken(anyString());
    }

    private void stubGetClientToken(final int expiresInSeconds) {
        when(tokensRestClient.getClientToken(anyString()))
                .thenReturn(new ClientTokenResponseDTO("client-token", expiresInSeconds));
        when(parser.parseClientToken("client-token")).thenReturn(new ClientToken("client-token", null));
    }

    private void stubGetClientGroupToken(final int expiresInSeconds) {
        when(tokensRestClient.getClientToken(anyString()))
                .thenReturn(new ClientTokenResponseDTO("client-group-token", expiresInSeconds));
        when(parser.parseClientToken("client-group-token")).thenReturn(new ClientGroupToken("client-group-token", null));
    }

    @Test
    void willNotRenewCacheWhenTokenHasNotAlmostExpired() {
        final int issuedSecondsAgo = 100;
        final int expiresInSeconds = 120;
        stubGetClientTokenWithSkewedClock(issuedSecondsAgo, expiresInSeconds);

        clientTokenService.getClientToken(CLIENT_ID);
        clientTokenService.getClientToken(CLIENT_ID);

        verify(tokensRestClient).getClientToken(anyString());
    }

    private void stubGetClientTokenWithSkewedClock(int issuedSecondsAgo, int expiresInSeconds) {
        Clock clock = Clock.fixed(Instant.now(Clock.systemDefaultZone()).minusSeconds(issuedSecondsAgo), ZoneId.systemDefault());
        clientTokenService = new ClientTokenRequesterService(tokensRestClient, signingKeyJwk, CLIENT_TOKEN_REQUESTER, parser, clock);
        when(tokensRestClient.getClientToken(anyString()))
                .thenReturn(new ClientTokenResponseDTO("client-token", issuedSecondsAgo + expiresInSeconds));
        when(parser.parseClientToken("client-token")).thenReturn(new ClientToken("client-token", null));
    }

    @Test
    void failsConstructorWhenJWKDoesNotContainAPrivateKey() throws Exception {
        RsaJsonWebKey rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
        String publicOnlyJwk = rsaJsonWebKey.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY);
        RsaJsonWebKey publicOnlyJsonWebKey = (RsaJsonWebKey) PublicJsonWebKey.Factory.newPublicJwk(publicOnlyJwk);
        assertThrows(
                IllegalStateException.class,
                () -> clientTokenService = new ClientTokenRequesterService(tokensRestClient, publicOnlyJsonWebKey, CLIENT_TOKEN_REQUESTER, parser));
    }
}
