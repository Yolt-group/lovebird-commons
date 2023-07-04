package com.yolt.service.starter.threadpool;

import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class CustomThreadPoolTaskExecutorWithPropertiesComponent extends ThreadPoolTaskExecutor {

    public CustomThreadPoolTaskExecutorWithPropertiesComponent(@Value("com.example.value") String value) {
        // Fails when beans are created prematurely.
        Assertions.assertNotNull(value);
    }

}
