package com.yolt.service.starter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(OutputCaptureExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = TestController.class)
class ValidationRejectedValueSuppressionTest {

    private static final String SENSITIVE_VALUE = "abcdefghij";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldNotOutputRejectedValueToStdout(CapturedOutput capturedOutput) throws Exception {

        ToBeValidatedComplexDTO dto = new ToBeValidatedComplexDTO(SENSITIVE_VALUE,
                new ToBeValidatedSimpleDTO(SENSITIVE_VALUE));

        mockMvc.perform(post("/f7268962-bac8-42f9-8f0b-de156f3a4615")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(dto)))
                .andExpect(status().is4xxClientError());

        assertThat(capturedOutput.getAll()).containsSubsequence("rejectedValue has been redacted");
        assertThat(capturedOutput.getAll()).doesNotContain(SENSITIVE_VALUE);
    }

    @SpringBootApplication
    static class TestApp {
        // Limit scope
    }
}

@RestController
class TestController {
    @PostMapping("/f7268962-bac8-42f9-8f0b-de156f3a4615")
    public ResponseEntity<ToBeValidatedComplexDTO> endpoint(@RequestBody @Valid final ToBeValidatedComplexDTO dto) {
        return ResponseEntity.ok(dto);
    }
}

@Data
@RequiredArgsConstructor
class ToBeValidatedComplexDTO {

    @Size(min = 1, max = 5)
    private final String s1;

    @Valid
    private final ToBeValidatedSimpleDTO c1;
}

@Data
@RequiredArgsConstructor
class ToBeValidatedSimpleDTO {

    @Size(min = 1, max = 5)
    private final String s2;
}

@Data
@RequiredArgsConstructor
class UselessDTO {

    @Size(min = 1, max = 5)
    private final String toplevelStr;

    @Valid
    private final UselessNestedDTO nested;
}

@Data
@RequiredArgsConstructor
class UselessNestedDTO {
    @Size(min = 1, max = 5)
    private final String nestedStr;
}
