package com.yolt.service.starter.http;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractSensitiveHeaderHandler {

    private final List<String> sensitiveHeaders;

    private static final String INTERNAL_DOMAIN_SUFFIX = ".cluster.local";

    private static boolean isInternalRequestAndNotStubs(final String host) {
        // It's an internal request if ends up with our internal domain suffix or it's a service name, no dots (e.g. 'kyc')
        // The exception is stubs, which should be treated as an external service
        return (host.endsWith(INTERNAL_DOMAIN_SUFFIX) && !host.contains("stubs")) ||
                (!host.contains(".") && !host.equalsIgnoreCase("stubs"));
    }

    protected boolean isAllowedToPropagate(String host, HttpHeaders httpHeaders) {
        return isInternalRequestAndNotStubs(host) || !containsSensitiveHeaders(httpHeaders.keySet());
    }

    protected SensitiveHeaderBlockedResponseVO createSensitiveRequestBlockedResponse(URI requestUri, HttpHeaders httpHeaders) {
        // Intentionally odd response code, to ensure external parties are not blamed incorrectly
        HttpStatus status = HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE;
        String baseUrl = requestUri.getScheme() + "://" + requestUri.getHost() + ":" + requestUri.getPort();
        log.error("External call to {} blocked because of sensitive headers; (path dropped because of possible sensitivity)", baseUrl); //NOSHERIFF base url is safe, it does not contain any sensitive parameters

        return new SensitiveHeaderBlockedResponseVO(
                status.value(),
                status.getReasonPhrase(),
                httpHeaders,
                requestUri.toString());
    }

    private boolean containsSensitiveHeaders(Set<String> headers) {
        return headers.stream()
                .map(String::toLowerCase)
                .anyMatch(h -> sensitiveHeaders.stream().map(String::toLowerCase).collect(toList()).contains(h));
    }
}
