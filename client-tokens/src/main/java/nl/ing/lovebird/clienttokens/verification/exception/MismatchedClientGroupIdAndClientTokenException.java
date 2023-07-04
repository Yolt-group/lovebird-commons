package nl.ing.lovebird.clienttokens.verification.exception;

public class MismatchedClientGroupIdAndClientTokenException extends IllegalArgumentException {
    public MismatchedClientGroupIdAndClientTokenException(String message) {
        super(message);
    }
}
