package nl.ing.lovebird.cassandra.autoconfigure;

import nl.ing.lovebird.cassandra.CassandraRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("java:S5838")
class YoltCassandra3RepositoryAutoConfigurationTest {

    private final CassandraRepository<?> cassandraRepository = Mockito.mock(CassandraRepository.class);

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(YoltCassandra3RepositoryAutoConfiguration.class))
            .withBean(
                    "testCassandraRepository",
                    CassandraRepository.class,
                    () -> cassandraRepository);

    @Test
    void does_not_enable_tracing_by_default() {
        contextRunner.run(c -> verify(cassandraRepository, never()).setTracingEnabled(anyBoolean()));
    }

    @Test
    void enables_tracing_when_property_is_set__true() {
        contextRunner
                .withPropertyValues("cassandra.tracing.enabled=true")
                .run(c -> verify(cassandraRepository, times(1)).setTracingEnabled(eq(true)));
    }

    @Test
    void enables_tracing_when_property_is_set__false() {
        contextRunner
                .withPropertyValues("cassandra.tracing.enabled=false")
                .run(c -> verify(cassandraRepository, times(1)).setTracingEnabled(eq(false)));
    }
}
