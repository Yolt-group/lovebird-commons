package nl.ing.lovebird.clienttokens.verification;

import nl.ing.lovebird.clienttokens.ClientUserToken;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserIdVerificationServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID OTHER_USER_ID = UUID.randomUUID();

    public static List<Arguments> test() {
        return Arrays.asList(
                // ( user-id in token, user-id, expect an exception )
                arguments(null, null, false),
                arguments(null, USER_ID, true),
                arguments(USER_ID, null, true),
                arguments(USER_ID, OTHER_USER_ID, true),
                arguments(USER_ID, USER_ID, false)
        );
    }

    @MethodSource
    @ParameterizedTest(name =  "[" + INDEX_PLACEHOLDER + "] verify({0},{1}) => exceptionThrown {2}")
    void test(UUID clientIdInClientToken,
              UUID clientId,
              boolean expectExceptionThrown) {
        // setup
        ClientUserToken clientToken = mockClientToken(clientIdInClientToken);
        UserIdVerificationService service = new UserIdVerificationService();

        if (expectExceptionThrown) {
            assertThrows(Exception.class, () -> service.verify(clientToken, clientId));
        } else {
            assertDoesNotThrow(() -> service.verify(clientToken, clientId));
        }
    }

    private ClientUserToken mockClientToken(UUID userIdInClientToken) {
        ClientUserToken clientUserToken = null;
        if (userIdInClientToken != null) {
            clientUserToken = mock(ClientUserToken.class);
            when(clientUserToken.getUserIdClaim()).thenReturn(userIdInClientToken);
        }
        return clientUserToken;
    }

}
