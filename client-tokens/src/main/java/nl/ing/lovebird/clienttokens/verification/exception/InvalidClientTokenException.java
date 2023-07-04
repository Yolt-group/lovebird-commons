package nl.ing.lovebird.clienttokens.verification.exception;

public class InvalidClientTokenException extends RuntimeException {
    public InvalidClientTokenException(Throwable cause) {
        super(cause);
    }

    public InvalidClientTokenException(String message) {
        super(message);
    }
}
