package com.yolt.sample.app;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class GreetingController {

    private final GreetingService service;

    @GetMapping(value = "/greet")
    public Mono<String> greet() {
        return service.helloAnonymousUser();
    }

    @GetMapping(value = "/greet/coffee")
    public Mono<String> greetTeapot() {
        return service.helloCoffeeUser();
    }


}
