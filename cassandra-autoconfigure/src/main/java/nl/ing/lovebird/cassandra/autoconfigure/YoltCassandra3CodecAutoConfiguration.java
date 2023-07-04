package nl.ing.lovebird.cassandra.autoconfigure;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.extras.codecs.jdk8.InstantCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@ConditionalOnClass({Cluster.class})
@AutoConfiguration
@SuppressWarnings("squid:S1118")
@Slf4j
public class YoltCassandra3CodecAutoConfiguration {

    @Bean
    public static BeanFactoryPostProcessor registerCassandraCodecs() {
        return new SmartLifecyclePostProcessor();
    }

    public static class SmartLifecyclePostProcessor implements BeanFactoryPostProcessor, SmartInitializingSingleton {

        private ConfigurableListableBeanFactory beanFactory;

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        @Override
        public void afterSingletonsInstantiated() {
            // We should register more codecs here in the future
            beanFactory.getBeansOfType(Cluster.class)
                    .forEach((beanName, cluster) ->
                            cluster.getConfiguration()
                                    .getCodecRegistry()
                                    .register(InstantCodec.instance));
        }

    }
}
