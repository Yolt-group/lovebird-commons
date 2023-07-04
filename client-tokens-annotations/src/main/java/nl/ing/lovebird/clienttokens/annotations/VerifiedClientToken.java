package nl.ing.lovebird.clienttokens.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Annotation to mark a parameter as a verified client-token.
 * This parameter is injected by the {@link VerifiedClientTokenParameterResolver}.
 * </p><p>
 * Methods with a parameter marked by this annotation will not be executed if:
 * (1) the client-token header is missing;
 * (2) the client-token is invalid;
 * (3) [optionally] the client-token is not issued for one of the specified token requesters.
 * A corresponding exception is thrown instead.
 * </p><p>
 * Example usage:
 * <pre>{@code
 *   @GetMapping public String protectedEndpoint(
 *       @VerifiedClientToken ClientToken clientToken
 *   ) {
 *       return String.format("Hello, %s", clientToken.getClientIdClaim());
 *   }
 * }</pre>
 *
 * If you want to restrict the allowed client-tokens to client-tokens that were issued for a
 * specific client-token requester, you can use the `restrictedTo` parameter. For example, when
 * you want to only allow clients to be able to call the endpoint using a client-token that was
 * issued for service "api-gateway", you could use:
 * <pre>{@code
 *   @GetMapping public String protectedAndRestrictedEndpoint(
 *       @VerifiedClientToken(restrictedTo = ClientTokenConstants.SERVICE_API_GATEWAY) ClientToken clientToken
 *   ) {
 *       return String.format("Hello, %s", clientToken.getClientIdClaim());
 *   }
 * }</pre>
 * </p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VerifiedClientToken {

    /**
     * To specify which client-token requesters are authorized during token verification.
     * Specifying an empty array means that no restrictions apply. Defaults to empty array.
     */
    String[] restrictedTo() default {};
}
