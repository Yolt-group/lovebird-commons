package com.yolt.sample.app;

import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Controller for use by {@link OutputSanitationIntegrationTest}.
 */
@RestController
public class ExceptionalController {

    @GetMapping(value = "/clientError")
    public Mono<String> clientError(
            @RequestHeader String accesstoken,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String missing) { // 400
        // Unreachable because of missing parameter
        return Mono.just(accesstoken + username + password + missing);
    }

    @GetMapping(value = "/serverError")
    public Mono<Void> serverError(
            @RequestHeader String accesstoken,
            @RequestParam String username,
            @RequestParam String password) {

        return Mono.error(new IllegalStateException("Some unknown exception that does not contain values")); // 500
    }

    @GetMapping(value = "/timeoutError")
    public Mono<String> timeoutError(
            @RequestHeader String accesstoken,
            @RequestParam String username,
            @RequestParam String password) {
        return Mono.never(); // Times out

    }

    @PostMapping(value = "/bodyError")
    public Mono<String> bodyError(
            @RequestHeader String accesstoken,
            @RequestParam String username,
            @RequestParam String password,
            @Valid @RequestBody SomeBody body) {
        return Mono.just(accesstoken + username + password + body.getName());
    }

    @Data
    public static class SomeBody {
        @NotNull
        String name;
    }
}
