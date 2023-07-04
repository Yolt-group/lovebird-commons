package nl.ing.lovebird.clienttokens.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.AbstractClientToken;
import nl.ing.lovebird.clienttokens.ClientGroupToken;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.clienttokens.annotations.AIS;
import nl.ing.lovebird.clienttokens.annotations.NonDeletedClient;
import nl.ing.lovebird.clienttokens.annotations.PIS;
import nl.ing.lovebird.clienttokens.annotations.VerifiedClientToken;
import nl.ing.lovebird.clienttokens.constants.ClientTokenConstants;
import nl.ing.lovebird.clienttokens.verification.ClientIdVerificationService;
import nl.ing.lovebird.clienttokens.verification.ClientTokenParser;
import nl.ing.lovebird.clienttokens.verification.ClientUserIdVerificationService;
import nl.ing.lovebird.clienttokens.verification.UserIdVerificationService;
import nl.ing.lovebird.clienttokens.verification.exception.InvalidClientTokenException;
import nl.ing.lovebird.clienttokens.verification.exception.MissingHeaderException;
import nl.ing.lovebird.clienttokens.verification.exception.UnauthorizedClientTokenClaimException;
import nl.ing.lovebird.clienttokens.verification.exception.UnauthorizedClientTokenRequesterException;
import nl.ing.lovebird.clienttokens.verification.sema.ClientTokenMissingSemaEvent;
import nl.ing.lovebird.logging.MDCContextCreator;
import nl.ing.lovebird.logging.SemaEventLogger;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * <p>
 * Parameter resolver for parameters marked with the {@link VerifiedClientToken} annotation.
 * </p><p>
 * It retrieves the client-token header and checks:
 * (1) that it is valid;
 * (2) that it is signed by tokens;
 * (3) [optionally] that the client-token is issued for one of the specified token requesters.
 * </p><p>
 * If the client-token header is missing, a {@link MissingHeaderException} is thrown.
 * If any of the checks fail an {@link InvalidClientTokenException} is thrown.
 * </p><p>
 * The claims are parsed and composed together with the original serialized client-token into a
 * {@link ClientUserToken} or {@link ClientToken} or {@link ClientGroupToken}. This object will be injected as the parameter value.
 * </p>
 */
@AllArgsConstructor
@Slf4j
public class VerifiedClientTokenParameterResolver implements HandlerMethodArgumentResolver {

    static final Class<VerifiedClientToken> ANNOTATION_TYPE = VerifiedClientToken.class;
    private static final String WHILE_CALLING_ENDPOINT = "%s while calling endpoint %s";
    private final ClientTokenParser parser;
    private final ClientIdVerificationService clientIdVerificationService;
    private final ClientUserIdVerificationService clientUserIdVerificationService;
    private final UserIdVerificationService userIdVerificationService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(ANNOTATION_TYPE);
    }

    @Override
    public AbstractClientToken resolveArgument(
            @NonNull MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            @NonNull NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        return tryResolveArgument(parameter, webRequest);
    }

    private AbstractClientToken tryResolveArgument(MethodParameter parameter, NativeWebRequest webRequest) {
        String clientTokenHeaderValue = webRequest.getHeader(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME);
        String clientIdHeaderValue = webRequest.getHeader(MDCContextCreator.CLIENT_ID_HEADER_NAME);
        String clientUserIdHeaderValue = webRequest.getHeader(MDCContextCreator.CLIENT_USER_ID_HEADER_NAME);
        String userIdHeaderValue = webRequest.getHeader(MDCContextCreator.USER_ID_HEADER_NAME);
        if (clientTokenHeaderValue == null) {
            SemaEventLogger.log(new ClientTokenMissingSemaEvent(clientIdHeaderValue, getRestrictedToList(parameter)));
            log.error("client-token header required");
            throw new MissingHeaderException("client-token header required");
        }
        AbstractClientToken token = this.parser.parseClientToken(clientTokenHeaderValue);
        if (token instanceof ClientToken && clientIdHeaderValue != null) {
            ClientToken clientToken = (ClientToken) token;
            this.clientIdVerificationService.verify(clientToken, UUID.fromString(clientIdHeaderValue));
        }
        if (token instanceof ClientUserToken && clientUserIdHeaderValue != null) {
            ClientUserToken clientToken = (ClientUserToken) token;
            this.clientUserIdVerificationService.verify(clientToken, UUID.fromString(clientUserIdHeaderValue));
        }
        if (token instanceof ClientUserToken && userIdHeaderValue != null) {
            ClientUserToken clientToken = (ClientUserToken) token;
            this.userIdVerificationService.verify(clientToken, UUID.fromString(userIdHeaderValue));
        }
        if (!this.isClientTokenRequesterAuthorized(parameter, token)) {
            ServletWebRequest request = webRequest.getNativeRequest(ServletWebRequest.class);
            String message = String.format("client-token with isf='%s' is not authorized, expected one of %s",
                    token.getIssuedForClaim(), getRestrictedToList(parameter));
            if (request != null) {
                message = String.format("%s for endpoint %s", message, request.getRequest().getServletPath());
            }
            throw new UnauthorizedClientTokenRequesterException(message);
        }
        checkAISClaim(parameter, webRequest, token);
        checkPISClaim(parameter, webRequest, token);
        checkDeletedClaim(parameter, webRequest, token);

        return token;
    }

    private boolean isClientTokenRequesterAuthorized(MethodParameter parameter, AbstractClientToken clientToken) {
        String issuedFor = clientToken.getIssuedForClaim();
        List<String> restrictedTo = getRestrictedToList(parameter);
        if (restrictedTo.isEmpty()) {
            // No restrictions apply
            return true;
        }
        return restrictedTo.contains(issuedFor);
    }

    private List<String> getRestrictedToList(MethodParameter parameter) {
        return Arrays.asList(Objects.requireNonNull(
                parameter.getParameterAnnotation(ANNOTATION_TYPE)
        ).restrictedTo());
    }

    private void checkAISClaim(MethodParameter parameter, NativeWebRequest webRequest, AbstractClientToken token) {
        AIS ais = parameter.getMethod().getAnnotation(AIS.class);
        if (ais != null && !(token instanceof ClientToken && ((ClientToken) token).hasAIS())) {
            ServletWebRequest request = webRequest.getNativeRequest(ServletWebRequest.class);
            String message = String.format("client-token for iss='%s' is not authorized for AIS", token.getSubject());
            if (request != null) {
                message = String.format(WHILE_CALLING_ENDPOINT, message, request.getRequest().getServletPath());
            }
            throw new UnauthorizedClientTokenClaimException(message);
        }
    }

    private void checkPISClaim(MethodParameter parameter, NativeWebRequest webRequest, AbstractClientToken token) {
        PIS pis = parameter.getMethod().getAnnotation(PIS.class);
        if (pis != null && !(token instanceof ClientToken && ((ClientToken) token).hasPIS())) {
            ServletWebRequest request = webRequest.getNativeRequest(ServletWebRequest.class);
            String message = String.format("client-token for iss='%s' is not authorized for PIS", token.getSubject());
            if (request != null) {
                message = String.format(WHILE_CALLING_ENDPOINT, message, request.getRequest().getServletPath());
            }
            throw new UnauthorizedClientTokenClaimException(message);
        }
    }

    private void checkDeletedClaim(MethodParameter parameter, NativeWebRequest webRequest, AbstractClientToken token) {
        NonDeletedClient nonDeletedClient = parameter.getMethod().getAnnotation(NonDeletedClient.class);
        if (nonDeletedClient != null && token instanceof ClientToken && ((ClientToken) token).isDeleted()) {
            ServletWebRequest request = webRequest.getNativeRequest(ServletWebRequest.class);
            String message = String.format("client-token for iss='%s' is for a deleted client", token.getSubject());
            if (request != null) {
                message = String.format(WHILE_CALLING_ENDPOINT, message, request.getRequest().getServletPath());
            }
            throw new UnauthorizedClientTokenClaimException(message);
        }
    }
}
