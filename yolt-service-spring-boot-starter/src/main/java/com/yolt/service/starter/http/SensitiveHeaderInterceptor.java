package com.yolt.service.starter.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.List;

/**
 * When using Sleuth + RestTemplate + external parties it's regrettably easy to send internal headers to external parties, as
 * `org.springframework.cloud.sleuth.instrument.web.client.TraceWebClientAutoConfiguration` creates a `TraceRestTemplateCustomizer`.
 * <p>
 * Developers should be aware that for calls to external parties they should disable Sleuth web instrumentation through either:
 * - configuration such as `spring.sleuth.web.client.enabled=false` (only when not also making internal HTTP calls)
 * - or create a non-bean(!) `RestTemplate` instance using a `new RestTemplateBuilder()`, not the autowired(!) instance.
 * <p>
 * If all of the above, and the code reviews have failed, this intercepter will blow up in an attempt to stop sensitive headers from
 * reaching external parties, by looking at the headers passed and the target host.
 */
@Slf4j
public class SensitiveHeaderInterceptor extends AbstractSensitiveHeaderHandler implements ClientHttpRequestInterceptor {

    public SensitiveHeaderInterceptor(List<String> sensitiveHeaders) {
        super(sensitiveHeaders);
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (isAllowedToPropagate(request.getURI().getHost(), request.getHeaders())) {
            return execution.execute(request, body);
        }
        return createSensitiveRequestBlockedResponse(request.getURI(), request.getHeaders());

    }
}