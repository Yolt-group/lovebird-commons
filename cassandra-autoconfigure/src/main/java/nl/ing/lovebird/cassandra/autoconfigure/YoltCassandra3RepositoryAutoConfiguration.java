package nl.ing.lovebird.cassandra.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.cassandra.CassandraRepository;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@ConditionalOnClass({CassandraRepository.class})
@AutoConfiguration
@SuppressWarnings("squid:S1118")
@Slf4j
public class YoltCassandra3RepositoryAutoConfiguration {

    @Bean
    public static BeanFactoryPostProcessor enableCassandraRepositoryTracing() {
        return new SmartLifecyclePostProcessor();
    }

    public static class SmartLifecyclePostProcessor implements BeanFactoryPostProcessor, SmartInitializingSingleton, EnvironmentAware {

        private ConfigurableListableBeanFactory beanFactory;
        private Environment environment;

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        @Override
        public void afterSingletonsInstantiated() {
            // All beans have created. We can safely look up executors without risking
            // premature instantiation. See BeanFactoryPostProcessor
            Boolean tracingEnabled = environment.getProperty("cassandra.tracing.enabled", Boolean.class);
            if (tracingEnabled != null) {
                beanFactory.getBeansOfType(CassandraRepository.class)
                        .forEach((beanName, repository) -> repository.setTracingEnabled(tracingEnabled));
            }
        }

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = environment;
        }
    }
}
