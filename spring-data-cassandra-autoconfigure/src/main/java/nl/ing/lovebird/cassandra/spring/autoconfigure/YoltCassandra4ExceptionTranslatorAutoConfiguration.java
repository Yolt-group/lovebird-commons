package nl.ing.lovebird.cassandra.spring.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.cql.CassandraAccessor;
import org.springframework.data.cassandra.core.cql.CassandraExceptionTranslator;
import org.springframework.data.cassandra.core.cql.CqlOperations;

/**
 * Autoconfigure cassandra exception translators.
 * <p>
 * Cassandra does not provide a feature to hide values from a CQL query in an
 * exceptions. As such we redact the entire cql string in the exception message.
 * <p>
 * The full CQL is still logged at DEBUG level.
 * <p>
 * https://datastax-oss.atlassian.net/browse/JAVA-2975
 */
@ConditionalOnClass(CassandraTemplate.class)
@AutoConfiguration
@Slf4j
@SuppressWarnings("squid:S1118")
public class YoltCassandra4ExceptionTranslatorAutoConfiguration {

    @Bean
    public static BeanPostProcessor redactCassandraExceptionCql() {
        return new CassandraTemplatePostProcessor();
    }

    public static class CassandraTemplatePostProcessor implements BeanPostProcessor {

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (!(bean instanceof CassandraTemplate)) {
                return bean;
            }
            CassandraTemplate template = (CassandraTemplate) bean;
            CqlOperations operations = template.getCqlOperations();

            if (!(operations instanceof CassandraAccessor)) {
                log.warn("Failed to install CqlRedactingCassandraExceptionTranslator on {}. CassandraTemplate.cqlOperations were not an instance of CassandraAccessor.", beanName);
                return bean;
            }

            CassandraAccessor accessor = (CassandraAccessor) operations;
            accessor.setExceptionTranslator(new CqlRedactingCassandraExceptionTranslator());
            return bean;
        }

        @Slf4j
        private static class CqlRedactingCassandraExceptionTranslator extends CassandraExceptionTranslator {
            @Override
            protected String buildMessage(String task, String cql, RuntimeException ex) {
                log.debug(super.buildMessage(task, cql, ex));
                return super.buildMessage(task, "redacted, see debug log", ex);
            }
        }
    }
}
