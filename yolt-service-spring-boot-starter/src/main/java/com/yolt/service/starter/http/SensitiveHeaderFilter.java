package com.yolt.service.starter.http;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

/**
 * This filter will blow up in an attempt to stop sensitive headers from
 * reaching external parties, by looking at the headers passed and the target host.
 */
@Slf4j
public class SensitiveHeaderFilter extends AbstractSensitiveHeaderHandler implements ExchangeFilterFunction {

    private final boolean dryRun;

    public SensitiveHeaderFilter(List<String> sensitiveHeaders, boolean dryRun) {
        super(sensitiveHeaders);
        this.dryRun = dryRun;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        URI requestUri = request.url();
        boolean allowedToPropagate = isAllowedToPropagate(requestUri.getHost(), request.headers());

        if(!allowedToPropagate && dryRun){
            String baseUrl = requestUri.getScheme() + "://" + requestUri.getHost() + ":" + requestUri.getPort();
            log.error("External call to {} blocked because of sensitive headers; (path dropped because of possible sensitivity)", baseUrl); //NOSHERIFF base url is safe, it does not contain any sensitive parameters
        }

        if (allowedToPropagate || dryRun) {
            return next.exchange(request);
        }
        ClientResponse blockedClientResponse = createSensitiveRequestBlockedResponse(createSensitiveRequestBlockedResponse(requestUri, request.headers()));
        return Mono.just(blockedClientResponse);

    }

    @SneakyThrows
    private ClientResponse createSensitiveRequestBlockedResponse(SensitiveHeaderBlockedResponseVO response) {
        return ClientResponse.create(response.getStatusCode())
                .body(response.getBody().toString())
                .headers(headers -> response.getHeaders()).build();
    }
}