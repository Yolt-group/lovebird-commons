package nl.ing.lovebird.cassandra.autoconfigure;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YoltCassandra3HealthContributorTest {

    @Mock
    Session session;
    @Mock
    ResultSet resultSet;

    @BeforeEach
    void setup() {
        when(session.execute(Mockito.any(Statement.class))).thenReturn(resultSet);
    }

    @Test
    void should_be_up_when_system_local_and_key_space_model_mutation_can_be_read() {
        HealthIndicator indicator = new YoltCassandra3HealthContributor("example", session);
        assertEquals(Status.UP, indicator.health().getStatus());
        assertEquals("accessible", indicator.health().getDetails().get("modelmutation"));
    }

    @Test
    void should_be_down_when_model_mutation_can_not_be_read() {
        HealthIndicator indicator = new YoltCassandra3HealthContributor("example", session);
        when(resultSet.all()).thenThrow(new RuntimeException("Cassandra session is disconnected"));
        assertEquals(Status.DOWN, indicator.health().getStatus());
    }

}
