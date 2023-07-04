package com.yolt.service.starter.http;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensitiveHeaderInterceptorTest {

    private static final String BLOCK_LISTED_HEADER_KEY = "request_trace_id";
    private static final String ALLOW_LISTED_HEADER_KEY = "non-sensitive";
    private final HttpHeaders BLOCK_LISTED_HEADERS = composeHeaders(BLOCK_LISTED_HEADER_KEY);
    private final HttpHeaders ALLOW_LISTED_HEADERS = composeHeaders(ALLOW_LISTED_HEADER_KEY);

    @Mock
    private ClientHttpResponse mockedResponse;

    @Mock
    private HttpRequest mockedRequest;

    private final SensitiveHeaderInterceptor subject = new SensitiveHeaderInterceptor(asList(BLOCK_LISTED_HEADER_KEY, "user-id", "traceid"));

    private static HttpHeaders composeHeaders(String key) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(key, "dummyValue");
        return headers;
    }

    @Test
    void testIntercept_InternalWithoutSensitiveHeaders() throws IOException {
        when(mockedRequest.getURI()).thenReturn(URI.create("http://site-management/site-management/hello"));
        when(mockedRequest.getHeaders()).thenReturn(ALLOW_LISTED_HEADERS);
        when(mockedResponse.getStatusCode()).thenReturn(HttpStatus.OK);

        ClientHttpResponse response = subject.intercept(mockedRequest, null, (req, body) -> mockedResponse);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
    }

    @Test
    void testIntercept_InternalWithSensitiveHeaders() throws IOException {
        when(mockedRequest.getURI()).thenReturn(URI.create("http://site-management/site-management/hello"));
        when(mockedRequest.getHeaders()).thenReturn(BLOCK_LISTED_HEADERS);
        when(mockedResponse.getStatusCode()).thenReturn(HttpStatus.OK);

        ClientHttpResponse response = subject.intercept(mockedRequest, null, (req, body) -> mockedResponse);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
    }

    @Test
    void testIntercept_InternalWithDomainSuffixWithSensitiveHeaders() throws IOException {
        when(mockedRequest.getURI()).thenReturn(URI.create("http://site-management.cluster.local/site-management/hello"));
        when(mockedRequest.getHeaders()).thenReturn(BLOCK_LISTED_HEADERS);
        when(mockedResponse.getStatusCode()).thenReturn(HttpStatus.OK);

        ClientHttpResponse response = subject.intercept(mockedRequest, null, (req, body) -> mockedResponse);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
    }

    @Test
    void testIntercept_ExternalWithoutSensitiveHeaders() throws IOException {
        when(mockedRequest.getURI()).thenReturn(URI.create("https://monzo.com/api"));
        when(mockedRequest.getHeaders()).thenReturn(ALLOW_LISTED_HEADERS);
        when(mockedResponse.getStatusCode()).thenReturn(HttpStatus.OK);

        ClientHttpResponse response = subject.intercept(mockedRequest, null, (req, body) -> mockedResponse);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
    }

    @Test
    void testIntercept_ExternalWithSensitiveHeaders() throws IOException {
        when(mockedRequest.getURI()).thenReturn(URI.create("https://monzo.com/api"));
        when(mockedRequest.getHeaders()).thenReturn(BLOCK_LISTED_HEADERS);

        ClientHttpResponse response = subject.intercept(mockedRequest, null, (req, body) -> mockedResponse);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE);
    }

    @Test
    void testIntercept_StubsWithoutSensitiveHeaders() throws IOException {
        when(mockedRequest.getURI()).thenReturn(URI.create("http://stubs/stubs/hello"));
        when(mockedRequest.getHeaders()).thenReturn(ALLOW_LISTED_HEADERS);
        when(mockedResponse.getStatusCode()).thenReturn(HttpStatus.OK);

        ClientHttpResponse response = subject.intercept(mockedRequest, null, (req, body) -> mockedResponse);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
    }

    @Test
    void testIntercept_StubsWithSensitiveHeaders() throws IOException {
        when(mockedRequest.getURI()).thenReturn(URI.create("http://stubs/stubs/hello"));
        when(mockedRequest.getHeaders()).thenReturn(BLOCK_LISTED_HEADERS);

        ClientHttpResponse response = subject.intercept(mockedRequest, null, (req, body) -> mockedResponse);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE);
    }

    @Test
    void testIntercept_StubsWithDomainSuffixWithSensitiveHeaders() throws IOException {
        when(mockedRequest.getURI()).thenReturn(URI.create("http://stubs.cluster.local/stubs/hello"));
        when(mockedRequest.getHeaders()).thenReturn(BLOCK_LISTED_HEADERS);

        ClientHttpResponse response = subject.intercept(mockedRequest, null, (req, body) -> mockedResponse);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE);
    }
}