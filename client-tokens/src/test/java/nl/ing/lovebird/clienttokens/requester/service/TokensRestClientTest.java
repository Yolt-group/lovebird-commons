package nl.ing.lovebird.clienttokens.requester.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokensRestClientTest {

    private static final String TOKENS_BASE_URL = "http://tokens";

    @Mock
    private RestTemplate restTemplate;

    private TokensRestClient tokensRestClient;

    @BeforeEach
    void before() {
        tokensRestClient = new TokensRestClient(restTemplate);
    }

    @Test
    void returnsTheClientToken() {
        String requestToken = "my-request-token";
        ClientTokenResponseDTO mockResponse = new ClientTokenResponseDTO("client-token", 60);
        when(restTemplate.postForObject("/client-token?request_token={token}", null, ClientTokenResponseDTO.class, requestToken))
                .thenReturn(mockResponse);

        ClientTokenResponseDTO actualResponse = tokensRestClient.getClientToken(requestToken);

        assertEquals(mockResponse, actualResponse);
    }

    @Test
    void returnsAnEmptyBody() {
        String requestToken = "my-request-token";
        when(restTemplate.postForObject("/client-token?request_token={token}", null, ClientTokenResponseDTO.class, requestToken))
                .thenReturn(null);

        assertThrows(IllegalStateException.class, () -> tokensRestClient.getClientToken(requestToken));

    }

    @Test
    void returnsAnErrorCode() {
        String requestToken = "my-request-token";
        when(restTemplate.postForObject("/client-token?request_token={token}", null, ClientTokenResponseDTO.class, requestToken))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(IllegalStateException.class, () -> tokensRestClient.getClientToken(requestToken));
    }
}