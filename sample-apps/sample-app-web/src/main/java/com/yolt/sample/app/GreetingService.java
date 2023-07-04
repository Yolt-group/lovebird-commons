package com.yolt.sample.app;

import org.springframework.stereotype.Service;

@Service
public class GreetingService {

    public String helloAnonymousUser() {
        return "Hello world";
    }

    public String helloCoffeeUser() throws TeapotException {
        throw new TeapotException("This is a teapot");
    }
}