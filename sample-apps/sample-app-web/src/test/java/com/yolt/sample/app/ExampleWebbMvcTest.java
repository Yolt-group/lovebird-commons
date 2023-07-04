package com.yolt.sample.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GreetingController.class)
class ExampleWebbMvcTest {
    @MockBean
    private GreetingService service;

    @Autowired
    private MockMvc mockmvc;

    @Test
    void testGreet() throws Exception {
        when(service.helloAnonymousUser()).thenReturn("hello mock");
        mockmvc.perform(get("/greet"))
                .andExpect(status().isOk())
                .andExpect(content().string("hello mock"));
    }

    @Test
    void testExceptionalGreet() throws Exception {
        when(service.helloCoffeeUser()).thenThrow(new TeapotException("mock pot"));

        mockmvc.perform(get("/greet/coffee"))
                .andExpect(status().is(418))
                .andExpect(jsonPath("$.code").value("EXAMPLE1008"))
                .andExpect(jsonPath("$.message").value("hello coffee drinker"));
    }
}
