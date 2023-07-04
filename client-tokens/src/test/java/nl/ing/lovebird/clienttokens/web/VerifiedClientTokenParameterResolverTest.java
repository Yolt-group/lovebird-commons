package nl.ing.lovebird.clienttokens.web;

import java.util.UUID;

import nl.ing.lovebird.clienttokens.TestTokenCreator;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.clienttokens.annotations.AIS;
import nl.ing.lovebird.clienttokens.annotations.NonDeletedClient;
import nl.ing.lovebird.clienttokens.annotations.VerifiedClientToken;
import nl.ing.lovebird.clienttokens.AbstractClientToken;
import nl.ing.lovebird.clienttokens.ClientGroupToken;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.constants.ClientTokenConstants;
import nl.ing.lovebird.clienttokens.verification.ClientIdVerificationService;
import nl.ing.lovebird.clienttokens.verification.ClientTokenParser;
import nl.ing.lovebird.clienttokens.verification.ClientUserIdVerificationService;
import nl.ing.lovebird.clienttokens.verification.UserIdVerificationService;
import nl.ing.lovebird.clienttokens.verification.exception.*;
import nl.ing.lovebird.logging.MDCContextCreator;
import org.assertj.core.api.Condition;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VerifiedClientTokenParameterResolverTest {

    private static final String CLIENT_ID = UUID.randomUUID().toString();
    private static final String CLIENT_USER_ID = UUID.randomUUID().toString();
    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String ISSUED_FOR_EXAMPLE = "providers";

    private VerifiedClientTokenParameterResolver subject;
    private ClientTokenParser parser;
    private MethodParameter parameter;
    private VerifiedClientToken annotation;
    private NativeWebRequest request;

    @BeforeEach
    void setup() throws Exception {
        this.parser = mock(ClientTokenParser.class);
        this.subject = new VerifiedClientTokenParameterResolver(parser, new ClientIdVerificationService(), new ClientUserIdVerificationService(), new UserIdVerificationService());
        this.request = mock(NativeWebRequest.class);
        this.parameter = mock(MethodParameter.class);
        this.annotation = mock(VerifiedClientToken.class);
        when(parameter.getParameterAnnotation(VerifiedClientTokenParameterResolver.ANNOTATION_TYPE))
                .thenReturn(annotation);
        when(parameter.getMethod()).thenReturn(this.getClass().getMethod("mockAISMethod", ClientToken.class));
    }

    public void mockGroupToken(@VerifiedClientToken ClientGroupToken clientToken) {
    }

    @AIS
    public void mockAISMethod(@VerifiedClientToken ClientToken clientToken) {
    }

    @NonDeletedClient
    public void mockNonDeletedClientMethod(@VerifiedClientToken ClientToken clientToken) {
    }

    @Test
    void onlyResolvesParametersThatAreAnnotatedByVerifiedClientToken() {
        MethodParameter methodParameter = mock(MethodParameter.class);
        when(methodParameter.hasParameterAnnotation(VerifiedClientToken.class))
                .thenReturn(true).thenReturn(false);
        assertThat(subject.supportsParameter(methodParameter)).isTrue();
        assertThat(subject.supportsParameter(methodParameter)).isFalse();
    }

    @Test
    void resolvesValidClientTokenHeaderToClientTokenWhenUnrestricted() {
        when(annotation.restrictedTo()).thenReturn(new String[0]);
        this.resolvesValidClientTokenHeaderToClientToken();
    }

    @Test
    void resolvesValidClientTokenHeaderToClientTokenWhenRestrictedToIsfClaimValue() {
        when(annotation.restrictedTo()).thenReturn(new String[]{ISSUED_FOR_EXAMPLE});
        this.resolvesValidClientTokenHeaderToClientToken();
    }

    @Test
    void resolvesValidClientGroupTokenHeaderToClientTokenWhenUnrestricted() throws Exception {
        when(annotation.restrictedTo()).thenReturn(new String[0]);
        when(parameter.getMethod()).thenReturn(this.getClass().getMethod("mockGroupToken", ClientGroupToken.class));
        this.resolvesValidClientTokenHeaderToClientGroupToken();
    }

    @Test
    void resolvesValidClientGroupTokenHeaderToClientTokenWhenRestrictedToIsfClaimValue()  throws Exception {
        when(annotation.restrictedTo()).thenReturn(new String[]{ISSUED_FOR_EXAMPLE});
        when(parameter.getMethod()).thenReturn(this.getClass().getMethod("mockGroupToken", ClientGroupToken.class));
        this.resolvesValidClientTokenHeaderToClientGroupToken();
    }

    @Test
    void resolvesValidClientUserTokenHeaderToClientTokenWhenUnrestricted() {
        when(annotation.restrictedTo()).thenReturn(new String[0]);
        this.resolvesValidClientTokenHeaderToClientUserToken();
    }

    @Test
    void resolvesValidClientUserTokenHeaderToClientTokenWhenRestrictedToIsfClaimValue() {
        when(annotation.restrictedTo()).thenReturn(new String[]{ISSUED_FOR_EXAMPLE});
        this.resolvesValidClientTokenHeaderToClientUserToken();
    }

    private void resolvesValidClientTokenHeaderToClientToken() {
        JwtClaims claims = TestTokenCreator.createJwtClaims();
        String serialized = "my-client-token";
        when(parser.parseClientToken(serialized)).thenReturn(new ClientToken(serialized, claims));
        when(request.getHeader(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME)).thenReturn(serialized);
        assertThat(subject.resolveArgument(parameter, null, request, null))
                .hasFieldOrPropertyWithValue("serialized", serialized)
                .has(new TheSameClaimsAs(claims));
    }

    private void resolvesValidClientTokenHeaderToClientGroupToken() {
        JwtClaims claims = TestTokenCreator.createJwtClaims();
        String serialized = "my-client-group-token";
        when(parser.parseClientToken(serialized)).thenReturn(new ClientGroupToken(serialized, claims));
        when(request.getHeader(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME)).thenReturn(serialized);
        assertThat(subject.resolveArgument(parameter, null, request, null))
                .hasFieldOrPropertyWithValue("serialized", serialized)
                .has(new TheSameClaimsAs(claims));
    }

    private void resolvesValidClientTokenHeaderToClientUserToken() {
        JwtClaims claims = TestTokenCreator.createJwtClaims();
        String serialized = "my-client-group-token";
        when(parser.parseClientToken(serialized)).thenReturn(new ClientUserToken(serialized, claims));
        when(request.getHeader(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME)).thenReturn(serialized);
        assertThat(subject.resolveArgument(parameter, null, request, null))
                .hasFieldOrPropertyWithValue("serialized", serialized)
                .has(new TheSameClaimsAs(claims));
    }

    @Test
    void throwsExceptionWhenClientTokenHeaderIsMissing() {
        when(request.getHeader(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME)).thenReturn(null);
        when(annotation.restrictedTo()).thenReturn(new String[]{ISSUED_FOR_EXAMPLE});
        assertThatThrownBy(() -> subject.resolveArgument(parameter, null, request, null))
                .isInstanceOf(MissingHeaderException.class);
    }

    @Test
    void throwsExceptionWhenClientTokenHasDifferentIsfClaimThanMethodWasRestrictedTo() {
        String serialized = "my-client-token";
        JwtClaims claims = TestTokenCreator.createJwtClaims();
        claims.setClaim(ClientTokenConstants.EXTRA_CLAIM_ISSUED_FOR, "WRONG");
        when(parser.parseClientToken(serialized)).thenReturn(new ClientToken(serialized, claims));
        when(request.getHeader(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME)).thenReturn(serialized);
        when(annotation.restrictedTo()).thenReturn(new String[]{ISSUED_FOR_EXAMPLE});
        assertThatThrownBy(() -> subject.resolveArgument(parameter, null, request, null))
                .isInstanceOf(UnauthorizedClientTokenRequesterException.class);
    }

    @Test
    void throwsExceptionWhenClientTokenIsMissingAISClaim() throws Exception {
        String serialized = "my-client-token";
        JwtClaims claims = TestTokenCreator.createJwtClaims();
        claims.setClaim(ClientTokenConstants.CLAIM_AIS, false);
        when(parser.parseClientToken(serialized)).thenReturn(new ClientToken(serialized, claims));
        when(request.getHeader(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME)).thenReturn(serialized);
        when(annotation.restrictedTo()).thenReturn(new String[0]);
        when(parameter.getMethod()).thenReturn(this.getClass().getMethod("mockAISMethod", ClientToken.class));
        assertThatThrownBy(() -> subject.resolveArgument(parameter, null, request, null))
                .isInstanceOf(UnauthorizedClientTokenClaimException.class);
    }

    @Test
    void throwsExceptionWhenClientTokenHasDeletedClaimForNonDeletedClientMethod() throws Exception {
        String serialized = "my-client-token";
        JwtClaims claims = TestTokenCreator.createJwtClaims();
        claims.setClaim(ClientTokenConstants.CLAIM_DELETED, true);
        when(parser.parseClientToken(serialized)).thenReturn(new ClientToken(serialized, claims));
        when(request.getHeader(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME)).thenReturn(serialized);
        when(annotation.restrictedTo()).thenReturn(new String[0]);
        when(parameter.getMethod()).thenReturn(this.getClass().getMethod("mockNonDeletedClientMethod", ClientToken.class));
        assertThatThrownBy(() -> subject.resolveArgument(parameter, null, request, null))
                .isInstanceOf(UnauthorizedClientTokenClaimException.class)
                .hasMessage("client-token for iss='client:11112222-3333-4444-5555-666677778888' is for a deleted client");
    }

    @Test
    void throwsNoExceptionWhenClientTokenHasNoDeletedClaimForNonDeletedClientMethod() throws Exception {
        String serialized = "my-client-token";
        JwtClaims claims = TestTokenCreator.createJwtClaims();
        claims.setClaim(ClientTokenConstants.CLAIM_DELETED, false);
        when(parser.parseClientToken(serialized)).thenReturn(new ClientToken(serialized, claims));
        when(request.getHeader(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME)).thenReturn(serialized);
        when(annotation.restrictedTo()).thenReturn(new String[0]);
        when(parameter.getMethod()).thenReturn(this.getClass().getMethod("mockNonDeletedClientMethod", ClientToken.class));
        AbstractClientToken token = subject.resolveArgument(parameter, null, request, null);
        assertThat(token).isNotNull();
    }

    /**
     * Client-id header is not mandatory, but when provided, it must match the client-token.
     */
    @Test
    void verifiesThatClientIdInTokenMatchesClientIdHeaderWhenProvided() {
        JwtClaims claims = TestTokenCreator.createJwtClaims();
        claims.setClaim(ClientTokenConstants.SUBJECT, "client:" + CLIENT_ID);
        claims.setClaim(ClientTokenConstants.EXTRA_CLAIM_CLIENT_ID, CLIENT_ID);
        String serialized = "my-client-token";
        when(parser.parseClientToken(serialized)).thenReturn(new ClientToken(serialized, claims));
        when(request.getHeader(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME)).thenReturn(serialized);
        when(annotation.restrictedTo()).thenReturn(new String[0]);

        // everything is fine when there is no client-id header value provided
        when(request.getHeader(MDCContextCreator.CLIENT_ID_HEADER_NAME)).thenReturn(null);
        subject.resolveArgument(parameter, null, request, null);

        // everything is fine when the client-id header value matches the client-id in the client-token header
        when(request.getHeader(MDCContextCreator.CLIENT_ID_HEADER_NAME)).thenReturn(CLIENT_ID);
        subject.resolveArgument(parameter, null, request, null);

        // exception is thrown when they do not match
        when(request.getHeader(MDCContextCreator.CLIENT_ID_HEADER_NAME))
                .thenReturn(UUID.randomUUID().toString());
        assertThatThrownBy(() -> subject.resolveArgument(parameter, null, request, null))
                .isInstanceOf(MismatchedClientIdAndClientTokenException.class);
    }

    /**
     * client-user-id header is not mandatory, but when provided, it must match the client-token.
     */
    @Test
    void verifiesThatClientUserIdInTokenMatchesClientIdHeaderWhenProvided() {
        JwtClaims claims = TestTokenCreator.createJwtClaims();
        claims.setClaim(ClientTokenConstants.SUBJECT, "client-user:" + CLIENT_USER_ID);
        claims.setClaim(ClientTokenConstants.EXTRA_CLAIM_CLIENT_USER_ID, CLIENT_USER_ID);
        String serialized = "my-client-token";
        when(parser.parseClientToken(serialized)).thenReturn(new ClientUserToken(serialized, claims));
        when(request.getHeader(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME)).thenReturn(serialized);
        when(annotation.restrictedTo()).thenReturn(new String[0]);

        // everything is fine when there is no client-id header value provided
        when(request.getHeader(MDCContextCreator.CLIENT_USER_ID_HEADER_NAME)).thenReturn(null);
        subject.resolveArgument(parameter, null, request, null);

        // everything is fine when the client-id header value matches the client-id in the client-token header
        when(request.getHeader(MDCContextCreator.CLIENT_USER_ID_HEADER_NAME)).thenReturn(CLIENT_USER_ID);
        subject.resolveArgument(parameter, null, request, null);

        // exception is thrown when they do not match
        when(request.getHeader(MDCContextCreator.CLIENT_USER_ID_HEADER_NAME)).thenReturn(UUID.randomUUID().toString());
        assertThatThrownBy(() -> subject.resolveArgument(parameter, null, request, null))
                .isInstanceOf(MismatchedClientUserIdAndClientTokenException.class);
    }

    /**
     * user-id header is not mandatory, but when provided, it must match the client-token.
     */
    @Test
    void verifiesThatUserIdInTokenMatchesClientIdHeaderWhenProvided() {
        JwtClaims claims = TestTokenCreator.createJwtClaims();
        claims.setClaim(ClientTokenConstants.SUBJECT, "client-user:" + CLIENT_USER_ID);
        claims.setClaim(ClientTokenConstants.EXTRA_CLAIM_USER_ID, USER_ID);
        String serialized = "my-client-token";
        when(parser.parseClientToken(serialized)).thenReturn(new ClientUserToken(serialized, claims));
        when(request.getHeader(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME)).thenReturn(serialized);
        when(annotation.restrictedTo()).thenReturn(new String[0]);

        // everything is fine when there is no client-id header value provided
        when(request.getHeader(MDCContextCreator.USER_ID_HEADER_NAME)).thenReturn(null);
        subject.resolveArgument(parameter, null, request, null);

        // everything is fine when the client-id header value matches the client-id in the client-token header
        when(request.getHeader(MDCContextCreator.USER_ID_HEADER_NAME)).thenReturn(USER_ID);
        subject.resolveArgument(parameter, null, request, null);

        // exception is thrown when they do not match
        when(request.getHeader(MDCContextCreator.USER_ID_HEADER_NAME)).thenReturn(UUID.randomUUID().toString());
        assertThatThrownBy(() -> subject.resolveArgument(parameter, null, request, null))
                .isInstanceOf(MismatchedUserIdAndClientTokenException.class);
    }

    private static class TheSameClaimsAs extends Condition<AbstractClientToken> {

        private final JwtClaims shouldEqual;

        TheSameClaimsAs(JwtClaims shouldEqual) {
            this.shouldEqual = shouldEqual;
        }

        @Override
        public boolean matches(AbstractClientToken value) {
            return value.getClaimsMap().equals(shouldEqual.getClaimsMap());
        }
    }
}
