package nl.ing.lovebird.clienttokens.verification.exception;

/**
 * Can be thrown when a client-token's claim is not set.
 */
public class UnauthorizedClientTokenClaimException extends RuntimeException {
    public UnauthorizedClientTokenClaimException(String message) {
        super(message);
    }
}
