package com.yolt.sample.cassandra;

import com.datastax.driver.core.Session;
import com.datastax.oss.driver.api.core.CqlSession;
import nl.ing.lovebird.cassandra.autoconfigure.YoltCassandra3AutoConfiguration;
import nl.ing.lovebird.cassandra.autoconfigure.YoltCassandra3ModelMutationAutoConfiguration;
import nl.ing.lovebird.cassandra.autoconfigure.test.YoltCassandra3ModelMutationTestAutoConfiguration;
import nl.ing.lovebird.cassandra.modelmutation.CassandraModelMutationApplier;
import nl.ing.lovebird.cassandra.spring.autoconfigure.YoltCassandra4ModelMutationAutoConfiguration;
import nl.ing.lovebird.cassandra.spring.autoconfigure.YoltCassandra4VaultAutoConfiguration;
import nl.ing.lovebird.cassandra.spring.autoconfigure.test.ApplyCassandraChangesFromCqlFilesV4;
import nl.ing.lovebird.cassandra.spring.autoconfigure.test.YoltCassandra4ModelMutationTestAutoConfiguration;
import nl.ing.lovebird.cassandra.test.TestCassandraCluster;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.autoconfigure.AutoConfigurations.of;

class CassandraV3AndSpringDataCombinedTest {

    private static String clusterHostName;
    private static Integer port;
    final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues(
                    "spring.data.cassandra.keyspace-name=example_app",
                    "spring.data.cassandra.local-datacenter=datacenter1",
                    "spring.data.cassandra.port=" + port,
                    "spring.data.cassandra.contactPoints=" + clusterHostName)
            .withConfiguration(
                    of(
                            // v3
                            YoltCassandra3AutoConfiguration.class,
                            YoltCassandra3ModelMutationAutoConfiguration.class,
                            YoltCassandra3ModelMutationTestAutoConfiguration.class,
                            // Spring Data
                            YoltCassandra4VaultAutoConfiguration.class,
                            YoltCassandra4ModelMutationAutoConfiguration.class,
                            YoltCassandra4ModelMutationTestAutoConfiguration.class,
                            CassandraAutoConfiguration.class
                    )
            );

    @BeforeAll
    static void setup() {
        clusterHostName = TestCassandraCluster.getClusterHost();
        port = TestCassandraCluster.getClusterPort();
    }

    @Test
    @DisplayName("[SHOULD] create beans and use version applier v4 [GIVEN] autoconfiguration for v3 and spring data (v4) are on the classpath")
    void test() {
        contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(Session.class)
                            .hasSingleBean(CqlSession.class)
                            .hasSingleBean(YoltCassandra3AutoConfiguration.class)
                            .doesNotHaveBean(YoltCassandra4VaultAutoConfiguration.class)
                            .hasSingleBean(CassandraModelMutationApplier.class);
                    assertThat(context.getBean(CassandraModelMutationApplier.class))
                            .isInstanceOf(ApplyCassandraChangesFromCqlFilesV4.class);
                });
    }

}
