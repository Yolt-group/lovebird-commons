package com.yolt.sample.app;

import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Controller for use by {@link OutputSanitationIntegrationTest}.
 */
@RestController
public class ExceptionalController {

    @GetMapping(value = "/clientError")
    public String clientError(
            @RequestHeader String accesstoken,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String missing) { // 400
        // Unreachable because of missing parameter
        return accesstoken + username + password + missing;
    }

    @GetMapping(value = "/serverError")
    public void serverError(
            @RequestHeader String accesstoken,
            @RequestParam String username,
            @RequestParam String password) {
        throw new IllegalStateException("Some unknown exception that does not contain values"); // 500
    }

    @GetMapping(value = "/timeoutError")
    public String timeoutError(
            @RequestHeader String accesstoken,
            @RequestParam String username,
            @RequestParam String password) throws InterruptedException {
        Thread.sleep(3000); // Times out to make below line unreachable
        return accesstoken + username + password;
    }

    @PostMapping(value = "/bodyError")
    public String bodyError(
            @RequestHeader String accesstoken,
            @RequestParam String username,
            @RequestParam String password,
            @Valid @RequestBody SomeBody body) {
        return accesstoken + username + password + body.getName();
    }

    @Data
    public static class SomeBody {
        @NotNull
        String name;
    }
}
