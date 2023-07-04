package com.yolt.sample.app;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GreetingService {

    public Mono<String> helloAnonymousUser() {
        return Mono.just("Hello world");
    }

    public Mono<String> helloCoffeeUser() {
        return Mono.error(new TeapotException("This is a teapot"));
    }
}