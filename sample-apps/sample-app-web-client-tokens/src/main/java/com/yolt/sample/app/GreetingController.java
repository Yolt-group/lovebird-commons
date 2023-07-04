package com.yolt.sample.app;

import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.clienttokens.annotations.VerifiedClientToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GreetingController {

    @GetMapping(value = "/greet")
    public String greet() {
        return "Hello world";
    }

    @GetMapping(value = "/greet/client-token")
    public String greetClient(@VerifiedClientToken ClientToken clientToken) {
        return "Hello " + clientToken.getClientIdClaim();
    }

    @GetMapping(value = "/greet/client-user-token")
    public String greetClientUser(@VerifiedClientToken ClientUserToken clientToken) {
        return "Hello " + clientToken.getUserIdClaim() + " from " + clientToken.getClientIdClaim();
    }

    @GetMapping(value = "/restricted/client-user-token")
    public String restrictedClientUser(@VerifiedClientToken(restrictedTo = "secret-service") ClientUserToken clientToken) {
        return "Hello secret agent " + clientToken.getUserIdClaim() + " from " + clientToken.getClientIdClaim();
    }


}
