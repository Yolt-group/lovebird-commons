package nl.ing.lovebird.clienttokens.verification;

import nl.ing.lovebird.clienttokens.AbstractClientToken;
import nl.ing.lovebird.clienttokens.verification.exception.MismatchedClientGroupIdAndClientTokenException;
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

class ClientGroupIdVerificationServiceTest {

    private static final UUID CLIENT_GROUP_ID = UUID.randomUUID();
    private static final UUID OTHER_CLIENT_GROUP_ID = UUID.randomUUID();

    public static List<Arguments> test() {
        return Arrays.asList(
                // ( client-id in token, client-id, expect an exception )
                arguments(null, null, false),
                arguments(null, CLIENT_GROUP_ID, true),
                arguments(CLIENT_GROUP_ID, null, true),
                arguments(CLIENT_GROUP_ID, OTHER_CLIENT_GROUP_ID, true),
                arguments(CLIENT_GROUP_ID, CLIENT_GROUP_ID, false)
        );
    }

    @MethodSource
    @ParameterizedTest(name = "[" + INDEX_PLACEHOLDER + "] verify({0},{1}) => exceptionThrown {2}")
    void test(UUID clientGroupIdInClientToken,
              UUID clientId,
              boolean expectExceptionThrown) {
        // setup
        AbstractClientToken clientToken = mockClientToken(clientGroupIdInClientToken);
        ClientGroupIdVerificationService service = new ClientGroupIdVerificationService();

        if (expectExceptionThrown) {
            assertThrows(MismatchedClientGroupIdAndClientTokenException.class, () -> service.verify(clientToken, clientId));
        } else {
            assertDoesNotThrow(() -> service.verify(clientToken, clientId));
        }
    }

    private AbstractClientToken mockClientToken(UUID clientGroupIdInClientToken) {
        AbstractClientToken clientToken = null;
        if (clientGroupIdInClientToken != null) {
            clientToken = mock(AbstractClientToken.class);
            when(clientToken.getClientGroupIdClaim()).thenReturn(clientGroupIdInClientToken);
        }
        return clientToken;
    }

}