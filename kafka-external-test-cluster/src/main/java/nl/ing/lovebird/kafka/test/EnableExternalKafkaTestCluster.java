package nl.ing.lovebird.kafka.test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable Kafka test cluster during tests.
 * <p>
 * When running tests locally enables a Kafka test container. Or when running in CI,
 * uses the service provided by CI. Note that all application contexts will share
 * the same cluster instance.
 * <p>
 * For usage see the the {@code sample-apps} module.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface EnableExternalKafkaTestCluster {
}
