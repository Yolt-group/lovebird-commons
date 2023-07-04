package nl.ing.lovebird.cassandra.autoconfigure.test;

import com.datastax.driver.core.Cluster;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.cassandra.autoconfigure.YoltCassandra3AutoConfiguration;
import nl.ing.lovebird.cassandra.autoconfigure.YoltCassandra3ModelMutationAutoConfiguration;
import nl.ing.lovebird.cassandra.autoconfigure.YoltCassandraProperties;
import nl.ing.lovebird.cassandra.modelmutation.CassandraModelMutationApplier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

/**
 * This AutoConfiguration makes sure that the CassandraUpdate scripts will be run on the cluster, regardless of how the cluster is created.
 */
@Conditional(YoltCassandra3ModelMutationAutoConfiguration.Condition.class)
@AutoConfiguration(before = YoltCassandra3ModelMutationAutoConfiguration.class, afterName = "nl.ing.lovebird.cassandra.spring.autoconfigure.test.YoltCassandra4ModelMutationTestAutoConfiguration")
@Slf4j
public class YoltCassandra3ModelMutationTestAutoConfiguration {

    /**
     * the session creator in {@link YoltCassandra3AutoConfiguration} depends on this bean.
     * the {@link YoltCassandra3ModelMutationAutoConfiguration} is waiting for cassandraUpdates to have run externally, but in this case we run them
     * manually and don't have to wait until everything has run. (so this should be configured before {@link YoltCassandra3ModelMutationAutoConfiguration}.)
     */
    @Bean
    @ConditionalOnMissingBean(CassandraModelMutationApplier.class)
    public CassandraModelMutationApplier applyCassandraChangesFromCqlFiles(Cluster cluster, YoltCassandraProperties properties) {
        return new ApplyCassandraChangesFromCqlFiles(cluster, properties.getKeyspaceName());
    }
}
