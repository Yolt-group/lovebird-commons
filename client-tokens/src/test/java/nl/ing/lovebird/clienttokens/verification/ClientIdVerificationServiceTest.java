package nl.ing.lovebird.clienttokens.verification;

import nl.ing.lovebird.clienttokens.ClientToken;
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

class ClientIdVerificationServiceTest {

    private static final UUID CLIENT_ID = UUID.randomUUID();
    private static final UUID OTHER_CLIENT_ID = UUID.randomUUID();

    public static List<Arguments> test() {
        return Arrays.asList(
                // ( client-id in token, client-id, expect an exception )
                arguments(null, null, false),
                arguments(null, CLIENT_ID, true),
                arguments(CLIENT_ID, null, true),
                arguments(CLIENT_ID, OTHER_CLIENT_ID, true),
                arguments(CLIENT_ID, CLIENT_ID, false)
        );
    }

    @MethodSource
    @ParameterizedTest(name =  "[" + INDEX_PLACEHOLDER + "] verify({0},{1}) => exceptionThrown {2}")
    void test(UUID clientIdInClientToken,
              UUID clientId,
              boolean expectExceptionThrown) {
        // setup
        ClientToken clientToken = mockClientToken(clientIdInClientToken);
        ClientIdVerificationService service = new ClientIdVerificationService();

        if (expectExceptionThrown) {
            assertThrows(Exception.class, () -> service.verify(clientToken, clientId));
        } else {
            assertDoesNotThrow(() -> service.verify(clientToken, clientId));
        }
    }

    private ClientToken mockClientToken(UUID clientIdInClientToken) {
        ClientToken clientToken = null;
        if (clientIdInClientToken != null) {
            clientToken = mock(ClientToken.class);
            when(clientToken.getClientIdClaim()).thenReturn(clientIdInClientToken);
        }
        return clientToken;
    }

}