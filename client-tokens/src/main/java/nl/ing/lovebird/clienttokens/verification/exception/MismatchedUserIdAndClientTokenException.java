package nl.ing.lovebird.clienttokens.verification.exception;

public class MismatchedUserIdAndClientTokenException extends IllegalArgumentException {
    public MismatchedUserIdAndClientTokenException(String message) {
        super(message);
    }
}
