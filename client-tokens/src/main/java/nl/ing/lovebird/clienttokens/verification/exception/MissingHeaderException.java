package nl.ing.lovebird.clienttokens.verification.exception;

public class MissingHeaderException extends RuntimeException {
    public MissingHeaderException(String message) {
        super(message);
    }
}
