package nl.ing.lovebird.clienttokens.verification.exception;

/**
 * Can be thrown when a client-token's `isf` (issued-for) claim does NOT match the
 * ClientTokenRequester configured in a @VerifiedClientToken's restrictedTo property.
 */
public class UnauthorizedClientTokenRequesterException extends RuntimeException {
    public UnauthorizedClientTokenRequesterException(String message) {
        super(message);
    }
}
