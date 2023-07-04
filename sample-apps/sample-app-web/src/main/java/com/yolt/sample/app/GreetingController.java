package com.yolt.sample.app;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GreetingController {

    private final GreetingService service;

    @GetMapping(value = "/greet")
    public String greet() {
        return service.helloAnonymousUser();
    }

    @GetMapping(value = "/greet/coffee")
    public String greetTeapot() throws TeapotException {
        return service.helloCoffeeUser();
    }


}
