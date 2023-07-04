package com.yolt.service.starter.http;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.cloud.sleuth.autoconfig.SleuthBaggageProperties;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Hooks;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getAllServeEvents;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;

class ReactiveHttpAutoConfigurationTest {

    // 'localhost' would be recognized as internal service name thus using 127.0.0.1 to tri k utilities to think this is external endpoint
    private static final String DUMMY_ENDPOINT = "http://127.0.0.1:8080/test";

    private static final WireMockServer wireMockServer = new WireMockServer();

    private final ApplicationContextRunner contextRunner = getContextRunner();

    @BeforeAll
    static void beforeAll() {
        // [Workaround start]
        // @see: https://github.com/spring-cloud/spring-cloud-sleuth/issues/1712
        Hooks.resetOnEachOperator();
        Hooks.resetOnLastOperator();
        Schedulers.resetOnScheduleHooks();
        // [Workaround end]

        wireMockServer.start();
        wireMockServer.givenThat(get(urlPathMatching("/test")).willReturn(ok()));
    }

    private ApplicationContextRunner getContextRunner() {
        return new ApplicationContextRunner()
                // mimic configuration of additionalSensitiveHeaders from application.yml
                .withPropertyValues("yolt.commons.additionalSensitiveHeaders=request_trace_id, Very_private_header")
                // mimic configuration of propagation-keys from application.yml
                .withBean(SleuthBaggageProperties.class, () -> {
                    SleuthBaggageProperties sleuthProperties = new SleuthBaggageProperties();
                    sleuthProperties.setRemoteFields(Collections.singletonList("x-sleuth-prohibited-header"));
                    return sleuthProperties;
                })
                // Drag in only minimum required auto-configurations: HttpAutoConfiguration, RestTemplateAutoConfiguration, WebClientAutoConfiguration
                .withConfiguration(AutoConfigurations.of(
                        ReactiveHttpAutoConfiguration.class,
                        WebClientAutoConfiguration.class
                ));
    }

    @AfterAll
    static void afterAll() {
        wireMockServer.stop();
    }

    @AfterEach
    void afterEach() {
        wireMockServer.resetRequests();
    }

    // Headers that we expect to be allowed and reach the external system
    private static Stream<Arguments> allowedHeaders() {
        return Stream.of(
                Arguments.of("request_trace_id2"),
                Arguments.of("Accepts")
        );
    }

    // Headers that we expect to be prohibited and should lead to error and no request propagation to external system
    private static Stream<Arguments> prohibitedHeaders() {
        return Stream.of(
                Arguments.of("request_trace_id"),
                Arguments.of("Very_private_header"),
                Arguments.of("x-sleuth-prohibited-header")
        );
    }

    @ParameterizedTest
    @MethodSource("allowedHeaders")
    @DisplayName("[Should] return OK [When] doing outbound request [Given] allowed headers only")
    void testWebClient(String headerUnderTest) {
        contextRunner
                .run(c -> {
            WebClient webClient = c.getBean(WebClient.Builder.class).build();

            ClientResponse response = webClient
                    .get()
                    .uri(DUMMY_ENDPOINT)
                    .header(headerUnderTest, "dummyValue")
                    .exchange()
                    .block();

            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);

            // Assert that request was propagated to the external system mimicked by Wiremock
            List<ServeEvent> allServeEvents = getAllServeEvents();
            assertThat(allServeEvents.get(0).getRequest().containsHeader(headerUnderTest)).isTrue();
        });
    }

    @ParameterizedTest
    @MethodSource("prohibitedHeaders")
    @DisplayName("[Should] return 431 [When] doing outbound request [Given] block-listed headers")
    void testWebClient2(String headerUnderTest) {
        contextRunner
                .run(c -> {
            WebClient webClient = c.getBean(WebClient.Builder.class).build();

            ClientResponse response = webClient
                    .get()
                    .uri(DUMMY_ENDPOINT)
                    .header(headerUnderTest, "dummyValue")
                    .exchange()
                    .block();

            assertThat(response.statusCode()).isEqualTo(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE);

            // Assert that request was not propagated to the external system mimicked by Wiremock
            assertThat(wireMockServer.getAllServeEvents()).isEmpty();
        });
    }

    @ParameterizedTest
    @MethodSource("prohibitedHeaders")
    @DisplayName("[Should] return 200 [When] doing outbound request in dry-run mode [Given] block-listed headers")
    void testWebClientDryRun(String headerUnderTest) {
        contextRunner
                .withPropertyValues("yolt.commons.block-sensitive-headers.dry-run=true")
                .run(c -> {
            WebClient webClient = c.getBean(WebClient.Builder.class).build();
            ClientResponse response = webClient
                    .get()
                    .uri(DUMMY_ENDPOINT)
                    .header(headerUnderTest, "dummyValue")
                    .exchange()
                    .block();

            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);

            // Assert that request was propagated to the external system mimicked by Wiremock
            List<ServeEvent> allServeEvents = getAllServeEvents();
            assertThat(allServeEvents.get(0).getRequest().containsHeader(headerUnderTest)).isTrue();
        });
    }

    @Test
    @DisplayName("[Should] create no WebClientCustomizer bean [When] running context[Given] WebClient is not on the class path")
    void testWebClient3() {
        contextRunner.withClassLoader(new FilteredClassLoader(WebClient.class))
                .run(context -> assertThat(context).doesNotHaveBean(WebClientCustomizer.class));
    }

    @Test
    @DisplayName("[Should] create only sensitive headers blocking WebClientCustomizer bean [When] running context[Given] SleuthProperties is not on the class path")
    void testWebClient4() {
        contextRunner.withClassLoader(new FilteredClassLoader(SleuthBaggageProperties.class))
                .run(context -> assertThat(context.getBeansOfType(WebClientCustomizer.class))
                        .containsOnlyKeys("webClientBlockSensitiveHeaders"));
    }

    @Test
    @DisplayName("[Should] create both sensitive headers and sleuth propagation keys blocking WebClientCustomizer bean [When] running context")
    void testWebClient5() {
        contextRunner
                .run(context -> assertThat(context.getBeansOfType(WebClientCustomizer.class))
                        .containsOnlyKeys("webClientBlockSensitiveHeaders", "webClientBlockSleuthPropagationKeys"));
    }
}