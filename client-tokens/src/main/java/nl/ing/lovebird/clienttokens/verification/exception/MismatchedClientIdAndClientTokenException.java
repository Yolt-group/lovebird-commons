package nl.ing.lovebird.clienttokens.verification.exception;

public class MismatchedClientIdAndClientTokenException extends IllegalArgumentException {
    public MismatchedClientIdAndClientTokenException(String message) {
        super(message);
    }
}
