package com.yolt.sample.postgres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * These classes demonstrate a typical postgres-backed application, and the various
 * ways to go around testing it. Used for reference in services, and to ensure
 * any starter changes do not break typical usage. Atypical usage is not
 * supported.
 */
@SpringBootApplication
public class ExampleApp {
    public static void main(String[] args) {
        SpringApplication.run(ExampleApp.class, args);
    }
}
