package com.yolt.service.starter.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.AbstractClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Getter
class SensitiveHeaderBlockedResponseVO extends AbstractClientHttpResponse {
    private final int rawStatusCode;
    private final String statusText;
    private final HttpHeaders headers;
    private final String body;

    @Override
    public InputStream getBody() {
        return new ByteArrayInputStream(body == null ? new byte[]{} : body.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void close() {
        // Nothing to close
    }
}