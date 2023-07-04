package com.yolt.service.starter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.autoconfigure.AutoConfigurations.of;

class JsonAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(of(
                    JsonAutoConfiguration.class,
                    JacksonAutoConfiguration.class
            ));

    @Test
    void shouldHaveRegisteredWellKnownModules() {
        contextRunner.run(context -> assertThat(context
                .getBean(ObjectMapper.class)
                .getRegisteredModuleIds())
                .containsExactlyInAnyOrder(
                        "com.fasterxml.jackson.datatype.jdk8.Jdk8Module",
                        "jackson-datatype-jsr310",
                        "jackson-module-parameter-names",
                        "org.springframework.boot.jackson.JsonComponentModule",
                        "org.springframework.boot.jackson.JsonMixinModule"
                )
        );
    }

    /**
     * Jackson 2.10 used "yyyy-MM-dd'T'HH:mm:ss.SSSZ" while 2.11+ uses "yyyy-MM-dd'T'HH:mm:ss.SSSX".
     * While the ISO 8601 zone format is superset of RFC 822 we don't want to spring this on our clients.
     */
    @Test
    void serializesDatesWithRfc822Zones() {
        contextRunner.run(context -> {
            ObjectMapper objectMapper = context.getBean(ObjectMapper.class);
            LocalDate localDate = LocalDate.of(1939, 11, 7);
            LocalTime localTime = LocalTime.of(0, 0);
            ZoneId zone = ZoneId.of("-0700");
            Date date = Date.from(ZonedDateTime.of(localDate, localTime, zone).toInstant());
            String jsonObject = objectMapper.convertValue(date, String.class);
            assertThat(jsonObject).isEqualTo("1939-11-07T07:00:00.000+0000");
        });
    }
}
