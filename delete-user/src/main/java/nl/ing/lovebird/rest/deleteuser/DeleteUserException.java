package nl.ing.lovebird.rest.deleteuser;

import java.util.Locale;

public class DeleteUserException extends RuntimeException {
    private DeleteUserException(final String message) {
        super(message);
    }

    public static DeleteUserException of(final DeleteUserResult result) {
        String message;
        if (result.getCount() == 0) {
            message = "No delete user functions defined.";
        } else {
            message = String.format(Locale.ROOT, "Only %s of %s delete functions were successful.", result.getSuccess(), result.getCount());
        }

        return new DeleteUserException(message);
    }
}
