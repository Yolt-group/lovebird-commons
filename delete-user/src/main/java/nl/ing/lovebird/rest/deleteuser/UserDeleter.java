package nl.ing.lovebird.rest.deleteuser;

import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.ClientUserToken;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
public class UserDeleter {

    private final List<Consumer<UUID>> deleteFunctionsByUserId = new ArrayList<>();
    private final List<Consumer<ClientUserToken>> deleteFunctionsByClientUserToken = new ArrayList<>();

    DeleteUserResult executeDeleteUser(final ClientUserToken clientUserToken) {
        UUID userId = clientUserToken.getUserIdClaim();
        if (deleteFunctionsByClientUserToken.isEmpty() && deleteFunctionsByUserId.isEmpty()) {
            log.error("Got delete request but no delete functions are defined!");
        } else {
            log.info("Deleting user {}", userId);
        }

        int successForUserId = (int) deleteFunctionsByUserId.stream()
                .filter(function -> delete(userId, function))
                .count();

        int successForClientUserToken = (int) deleteFunctionsByClientUserToken.stream()
                .filter(function -> delete(clientUserToken, function))
                .count();

        return new DeleteUserResult(deleteFunctionsByClientUserToken.size() + deleteFunctionsByUserId.size(), successForUserId + successForClientUserToken);
    }

    private <T> boolean delete(final T id, final Consumer<T> function) {
        try {
            function.accept(id);
            return true;
        } catch (Exception e) {
            log.error("Error calling delete function: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * register a delete function to be called when a DELETE request is made to the {@link DeleteUserController}.
     * The delete function should be idempotent
     *
     * @param function {@link Consumer} that takes a userId {@link UUID} that handles the delete
     * @deprecated user {@link #registerDeleter(Consumer)} that accepts a secure client user token.
     */
    @Deprecated
    public void register(final Consumer<UUID> function) {
        deleteFunctionsByUserId.add(function);
    }

    public void registerDeleter(final Consumer<ClientUserToken> function) {
        deleteFunctionsByClientUserToken.add(function);
    }

}
