package nl.ing.lovebird.clienttokens.verification.exception;

public class MismatchedClientUserIdAndClientTokenException extends IllegalArgumentException {
    public MismatchedClientUserIdAndClientTokenException(String message) {
        super(message);
    }
}
