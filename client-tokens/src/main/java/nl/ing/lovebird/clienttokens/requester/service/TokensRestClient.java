package nl.ing.lovebird.clienttokens.requester.service;

import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.logging.AuditLogger;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class TokensRestClient {

    private final RestTemplate restTemplate;

    public TokensRestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ClientTokenResponseDTO getClientToken(String requestToken) {
        try {
            ClientTokenResponseDTO response = restTemplate.postForObject("/client-token?request_token={token}", null, ClientTokenResponseDTO.class, requestToken);
            if (response == null) {
                throw new IllegalStateException("Got a 2xx code with a null body back while requesting a client-token");
            }
            log.info("Received a client token with a validity of {} seconds", response.getExpiresIn());
            AuditLogger.logSuccess("Received a client token by a requestToken: " + requestToken, response.getClientToken());
            return response;
        } catch (HttpStatusCodeException e) {
            AuditLogger.logError("Failed receiving a client token by requestToken", requestToken, e);
            throw new IllegalStateException("Failed retrieving a client token by requestToken", e);
        }
    }
}
