package nl.ing.lovebird.cassandra.spring.autoconfigure.test;

import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.cassandra.modelmutation.CassandraModelMutationApplier;
import nl.ing.lovebird.cassandra.spring.autoconfigure.YoltCassandra4ModelMutationAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

/**
 * This AutoConfiguration makes sure that the CassandraUpdate scripts will be run on the cluster, regardless of how the cluster is created.
 */
@Conditional(YoltCassandra4ModelMutationAutoConfiguration.Condition.class)
@AutoConfiguration(before = YoltCassandra4ModelMutationAutoConfiguration.class)
@Slf4j
public class YoltCassandra4ModelMutationTestAutoConfiguration {

    /**
     * the session creator in {@link YoltCassandra4ModelMutationAutoConfiguration} depends on this bean.
     * the {@link YoltCassandra4ModelMutationAutoConfiguration} is waiting for cassandraUpdates to have run externally, but in this case we run them
     * manually and don't have to wait until everything has run. (so this should be configured before {@link YoltCassandra4ModelMutationAutoConfiguration}.)
     */
    @Bean
    @ConditionalOnMissingBean(CassandraModelMutationApplier.class)
    public CassandraModelMutationApplier applyCassandraVChangesFromCqlFilesV4(CqlSessionBuilder cqlSessionBuilder, CassandraProperties properties) {
        return new ApplyCassandraChangesFromCqlFilesV4(cqlSessionBuilder, properties.getKeyspaceName());
    }
}

