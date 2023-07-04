package com.yolt.service.starter.test.autoconfigure;

import kafka.server.KafkaServer;
import kafka.server.ReplicaManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import java.lang.reflect.Field;
import java.util.Optional;

@ConditionalOnBean(EmbeddedKafkaBroker.class)
@AutoConfiguration
@Slf4j
@SuppressWarnings("squid:S3011")
public class EmbeddedKafkaShutdownAutoConfiguration implements ApplicationListener<ContextClosedEvent> {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Override
    public void onApplicationEvent(final ContextClosedEvent event) {
        // Fix for Kafka/Surefire race condition.
        //
        // See: https://github.com/spring-projects/spring-kafka/issues/194
        //
        // When using embedded Kafka there is a race condition that will prematurely
        // exit surefire. This can be avoided by shutting down embedded kafka when the application context is closed.

        try {
            final Optional<KafkaServer> serverOptional = embeddedKafkaBroker.getKafkaServers().stream().findFirst();
            if (serverOptional.isPresent()) {
                KafkaServer kafkaServer = serverOptional.get();
                ReplicaManager replicaManager = kafkaServer.replicaManager();
                if (replicaManager != null) {
                    replicaManager.shutdown(false);
                    final Field replicaManagerField = kafkaServer.getClass().getDeclaredField("replicaManager");
                    if (replicaManagerField != null) {
                        replicaManagerField.setAccessible(true);
                        replicaManagerField.set(kafkaServer, null);
                    }
                }
            }
            embeddedKafkaBroker.destroy();
        } catch (Exception e) {
            log.error("Shutdown embedded kafka failed.", e);
        }
    }
}

