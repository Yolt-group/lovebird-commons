package nl.ing.lovebird.rest.deleteuser;

import nl.ing.lovebird.clienttokens.ClientUserToken;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

class UserDeleterTest {

    private final ClientUserToken clientUserToken = mock(ClientUserToken.class);
    @Test
    void deleteUserWithoutRegisteredFunctions() {
        final UserDeleter userDeleter = new UserDeleter();

        final DeleteUserResult deleteUserResult = userDeleter.executeDeleteUser(clientUserToken);

        assertThat(deleteUserResult.getCount(), is(0L));
        assertThat(deleteUserResult.getSuccess(), is(0L));
    }

    @Test
    void deleteUser() {
        final UserDeleter userDeleter = new UserDeleter();
        userDeleter.register(System.out::println);

        final DeleteUserResult deleteUserResult = userDeleter.executeDeleteUser(clientUserToken);

        assertThat(deleteUserResult.getCount(), is(1L));
        assertThat(deleteUserResult.getSuccess(), is(1L));
    }


    @Test
    void deleteUserThrowsException() {
        final UserDeleter userDeleter = new UserDeleter();
        userDeleter.register(uuid -> {
            throw new RuntimeException(uuid.toString());
        });

        final DeleteUserResult deleteUserResult = userDeleter.executeDeleteUser(clientUserToken);

        assertThat(deleteUserResult.getCount(), is(1L));
        assertThat(deleteUserResult.getSuccess(), is(0L));
        assertFalse(deleteUserResult.isSuccess());
    }
}