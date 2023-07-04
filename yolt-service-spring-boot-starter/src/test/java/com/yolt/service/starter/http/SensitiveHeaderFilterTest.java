package com.yolt.service.starter.http;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SensitiveHeaderFilterTest {

    public static final String BLOCK_LISTED_HEADER_KEY = "request_trace_id";
    public static final String ALLOW_LISTED_HEADER_KEY = "non-sensitive";
    public static final String DUMMY_HEADER_VALUE = "dummyValue";
    private static final List<String> SENSITIVE_HEADERS_CONFIG = Arrays.asList("request_trace_id", "user-id", "traceid");

    private final SensitiveHeaderFilter filter = new SensitiveHeaderFilter(SENSITIVE_HEADERS_CONFIG, false);

    @Test
    void testFilter_InternalWithoutSensitiveHeaders() {
        ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("http://site-management/site-management/hello"))
                .header(ALLOW_LISTED_HEADER_KEY, DUMMY_HEADER_VALUE)
                .build();

        ClientResponse response = filter.filter(request, (req) -> Mono.just(ClientResponse.create(HttpStatus.OK).build())).block();

        assertThat(response.statusCode()).isEqualByComparingTo(HttpStatus.OK);
    }

    @Test
    void testFilter_InternalWithSensitiveHeaders() {
        ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("http://site-management/site-management/hello"))
                .header(BLOCK_LISTED_HEADER_KEY, DUMMY_HEADER_VALUE)
                .build();

        ClientResponse response = filter.filter(request, (req) -> Mono.just(ClientResponse.create(HttpStatus.OK).build())).block();

        assertThat(response.statusCode()).isEqualByComparingTo(HttpStatus.OK);
    }

    @Test
    void testFilter_InternalWithDomainSuffixWithSensitiveHeaders() {
        ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("http://site-management.cluster.local/site-management/hello"))
                .header(BLOCK_LISTED_HEADER_KEY, DUMMY_HEADER_VALUE)
                .build();

        ClientResponse response = filter.filter(request, (req) -> Mono.just(ClientResponse.create(HttpStatus.OK).build())).block();

        assertThat(response.statusCode()).isEqualByComparingTo(HttpStatus.OK);
    }

    @Test
    void testFilter_ExternalWithoutSensitiveHeaders() {
        ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("http://site-management.cluster.local/site-management/hello"))
                .header(ALLOW_LISTED_HEADER_KEY, DUMMY_HEADER_VALUE)
                .build();

        ClientResponse response = filter.filter(request, (req) -> Mono.just(ClientResponse.create(HttpStatus.OK).build())).block();
        assertThat(response.statusCode()).isEqualByComparingTo(HttpStatus.OK);
    }

    @Test
    void testFilter_ExternalWithSensitiveHeaders() {
        ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("https://monzo.com/api"))
                .header(BLOCK_LISTED_HEADER_KEY, DUMMY_HEADER_VALUE)
                .build();

        ClientResponse response = filter.filter(request, (req) -> Mono.just(ClientResponse.create(HttpStatus.OK).build())).block();

        assertThat(response.statusCode()).isEqualByComparingTo(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE);
    }

    @Test
    void testFilter_ExternalWithSensitiveHeaders_dryRun() {
        SensitiveHeaderFilter filter = new SensitiveHeaderFilter(SENSITIVE_HEADERS_CONFIG, true);

        ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("https://monzo.com/api"))
                .header(BLOCK_LISTED_HEADER_KEY, DUMMY_HEADER_VALUE)
                .build();

        ClientResponse response = filter.filter(request, (req) -> Mono.just(ClientResponse.create(HttpStatus.OK).build())).block();

        assertThat(response.statusCode()).isEqualByComparingTo(HttpStatus.OK);
    }

    @Test
    void testFilter_StubsWithoutSensitiveHeaders() {
        ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("http://stubs/stubs/hello"))
                .header(ALLOW_LISTED_HEADER_KEY, DUMMY_HEADER_VALUE)
                .build();

        ClientResponse response = filter.filter(request, (req) -> Mono.just(ClientResponse.create(HttpStatus.OK).build())).block();

        assertThat(response.statusCode()).isEqualByComparingTo(HttpStatus.OK);
    }

    @Test
    void testFilter_StubsWithSensitiveHeaders() {
        ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("http://stubs/stubs/hello"))
                .header(BLOCK_LISTED_HEADER_KEY, DUMMY_HEADER_VALUE)
                .build();

        ClientResponse response = filter.filter(request, (req) -> Mono.just(ClientResponse.create(HttpStatus.OK).build())).block();

        assertThat(response.statusCode()).isEqualByComparingTo(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE);
    }

    @Test
    void testFilter_StubsWithDomainSuffixWithSensitiveHeaders() {
        ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("http://stubs.cluster.local/stubs/hello"))
                .header(BLOCK_LISTED_HEADER_KEY, DUMMY_HEADER_VALUE)
                .build();

        ClientResponse response = filter.filter(request, (req) -> Mono.just(ClientResponse.create(HttpStatus.OK).build())).block();

        assertThat(response.statusCode()).isEqualByComparingTo(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE);
    }
}