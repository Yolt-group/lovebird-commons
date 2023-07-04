package nl.ing.lovebird.clienttokens.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Annotation to mark a method specific for AIS.
 * </p><p>
 * Methods marked by this annotation will not be executed if:
 * (1) the client-token header is missing;
 * (2) the client-token is invalid;
 * (3) the client-token does not have the claim AIS.
 * A corresponding exception is thrown instead.
 * </p><p>
 * Example usage:
 * <pre>{@code
 *   @AIS
 *   @GetMapping public String protectedEndpoint(
 *       @VerifiedClientToken ClientToken clientToken
 *   ) {
 *       return String.format("Hello, %s", clientToken.getClientIdClaim());
 *   }
 * }</pre>
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AIS {
}
