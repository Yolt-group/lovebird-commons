package com.yolt.service.starter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;

@ConditionalOnClass(ObjectMapper.class)
@AutoConfiguration(before = JacksonAutoConfiguration.class)
public class JsonAutoConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperBuilderCustomizer() {
        return builder -> builder.featuresToDisable(
                // Prevent user data snippets ending up in the logs on JsonParseExceptions
                JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION,
                // Format Dates instead of returning a long
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                // Tolerate new fields
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                // Do not normalize time zone to UTC
                DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                // Use Jackson 2.10 timezone format (RFC 822) rather then ISO 8601.
                .dateFormat(new StdDateFormat().withColonInTimeZone(false));
    }

}
