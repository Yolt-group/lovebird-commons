package com.yolt.teamstarter;

import com.yolt.service.starter.YoltWebServerVaultAutoConfiguration;
import com.yolt.service.starter.vault.YoltVaultProperties;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import nl.ing.lovebird.cassandra.autoconfigure.YoltCassandraProperties;
import nl.ing.lovebird.cassandra.autoconfigure.YoltVaultCassandraProperties;
import nl.ing.lovebird.kafka.autoconfigure.YoltVaultSecretsProperties;
import nl.ing.lovebird.postgres.autoconfigure.YoltVaultPostgresProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;


/**
 * Start the application locally connected to a team environment. At the moment this configuration:
 * <p>
 * - Downloads the contents of /vault/secrets to your local machine
 * - Retrieves config map for Kafka bootstrap server and sets the correct property
 * - Retrieves config map for Cassandra for the contact points and configures them
 */
@ConditionalOnClass({KubernetesClient.class})
@AutoConfiguration(before = YoltWebServerVaultAutoConfiguration.class)
@EnableConfigurationProperties(TeamStarterProperties.class)
@PropertySource(value = "https://ingress.${yolt.team.starter.environment}.yolt.io/config-server/${yolt.team.starter.application-name}-${yolt.team.starter.namespace}.properties")
@SuppressWarnings("squid:S1118")
public class TeamStarterAutoConfiguration {

    @Bean
    public static BeanFactoryPostProcessor applyYoltVaultSecretsPropertiesPostProcessor(Environment environment, @Value("${java.io.tmpdir}") Path tempDir) {
        return new YoltVaultSecretsPropertiesPostProcessor(bindPropertiesTo(environment), tempDir);
    }

    @Bean
    public static BeanFactoryPostProcessor applyYoltVaultPropertiesPostProcessor(Environment environment, @Value("${java.io.tmpdir}") Path tempDir) {
        return new YoltVaultPropertiesPostProcessor(bindPropertiesTo(environment), tempDir);
    }

    @Bean
    public static BeanFactoryPostProcessor applyYoltVaultPostgresPropertiesPostProcessor(Environment environment, @Value("${java.io.tmpdir}") Path tempDir) {
        return new YoltVaultPostgresPropertiesPostProcessor(bindPropertiesTo(environment), tempDir);
    }

    @Bean
    @ConditionalOnClass(name = "nl.ing.lovebird.cassandra.spring.autoconfigure.YoltVaultCassandraProperties")
    public static BeanFactoryPostProcessor applyYoltVaultSpringCassandraPropertiesPostProcessor(Environment environment, @Value("${java.io.tmpdir}") Path tempDir) {
        return new YoltVaultSpringCassandraPropertiesPostProcessor(bindPropertiesTo(environment), tempDir);
    }

    @Bean
    @ConditionalOnClass(name = "nl.ing.lovebird.cassandra.autoconfigure.YoltVaultCassandraProperties")
    public static BeanFactoryPostProcessor applyYoltVaultCassandraPropertiesPostProcessor(Environment environment, @Value("${java.io.tmpdir}") Path tempDir) {
        return new YoltVaultCassandraPropertiesPostProcessor(bindPropertiesTo(environment), tempDir);
    }

    @Bean
    @ConditionalOnClass(name = "nl.ing.lovebird.cassandra.autoconfigure.YoltCassandraProperties")
    public static BeanFactoryPostProcessor applyYoltCassandraPropertiesPostProcessor(Environment environment, @Value("${java.io.tmpdir}") Path tempDir) {
        return new YoltCassandraPropertiesPostProcessor(bindPropertiesTo(environment), tempDir);
    }

    @Bean
    public static BeanFactoryPostProcessor applyCassandraPropertiesPostProcessor(Environment environment, @Value("${java.io.tmpdir}") Path tempDir) {
        return new CassandraPropertiesPostProcessor(bindPropertiesTo(environment), tempDir);
    }

    @Bean
    public static BeanFactoryPostProcessor applyKafkaPropertiesPostProcessor(Environment environment, @Value("${java.io.tmpdir}") Path tempDir) {
        return new KafkaPropertiesPostProcessor(bindPropertiesTo(environment), tempDir);
    }

    private static TeamStarterProperties bindPropertiesTo(Environment environment) {
        // We can't use @ConfigurationProperties in a BeanFactoryPostProcessor but we do want
        // the ability to automatically generate the additional-spring-configuration-metadata.json file
        // through spring-boot-configuration-processor.
        //
        // So that's why we bind it ourselves.
        // https://stackoverflow.com/questions/53851533/access-properties-in-beanfactorypostprocessor
        BindResult<TeamStarterProperties> configuration = Binder.get(environment)
                .bind("yolt.team.starter", TeamStarterProperties.class);
        TeamStarterProperties properties = configuration.get();
        return properties;
    }

    private abstract static class TypeSafeConfigurationPropertiesBindingPostProcessor<T> extends ConfigurationPropertiesBindingPostProcessor {

        private final Class<T> type;

        TypeSafeConfigurationPropertiesBindingPostProcessor(Class<T> type) {
            this.type = type;
        }

        @Override
        public final Object postProcessAfterInitialization(Object bean, String beanName) {
            //If we do this in postProcessBeanFactory Spring did not yet fully initialize the properties so we
            //miss the config-server properties, for example the keyspace etc.
            if (type.isInstance(bean)) {
                return safelyPostProcessAfterInitialization(type.cast(bean), beanName);
            }
            return bean;
        }

        abstract Object safelyPostProcessAfterInitialization(T bean, String beanName);
    }

    private abstract static class TeamStarterPostProcessor<T> extends TypeSafeConfigurationPropertiesBindingPostProcessor<T> implements BeanFactoryPostProcessor {

        protected final String namespace;
        private final String context;
        private final String applicationName;
        private final String containerName;
        private final Path tempDir;

        TeamStarterPostProcessor(Class<T> type, TeamStarterProperties properties, Path tempDir) {
            super(type);
            this.context = requireNonNull(properties.getKubernetesContext(), "kubernetes-context should not be null");
            this.namespace = requireNonNull(properties.getNamespace(), "namespace should not be null");
            this.applicationName = requireNonNull(properties.getApplicationName(), "application-name should not be null");
            this.containerName = StringUtils.hasText(properties.getContainerName())
                    ? properties.getContainerName()
                    : applicationName;
            this.tempDir = tempDir;
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
            downloadVaultSecrets();
        }

        private void downloadVaultSecrets() {
            try (KubernetesClient client = getForClusterAndEnv()) {
                Pod pod = findRunningPodOnEnvironment(client);
                downloadVaultSecrets(client, pod);
            }
        }

        protected KubernetesClient getForClusterAndEnv() {
            KubernetesContextValidator.validate(context);
            final Config config = Config.autoConfigure(context);
            return new DefaultKubernetesClient(config);
        }

        protected String getCassandraContactPointsFromConfigMap(KubernetesClient client) {
            Map<String, String> cassandraConfigMap = client.configMaps()
                    .inNamespace(namespace)
                    .withName("cassandra")
                    .get()
                    .getData();
            return cassandraConfigMap.get("spring_data_cassandra_contact_points");
        }

        protected List<String> getKafkaBootStrapServersFromConfigMap(KubernetesClient client) {
            Map<String, String> kafkaConfigMap = client.configMaps()
                    .inNamespace(namespace)
                    .withName("kafka")
                    .get()
                    .getData();
            return singletonList(kafkaConfigMap.get("spring_kafka_bootstrap_servers"));
        }

        private void downloadVaultSecrets(KubernetesClient client, Pod pod) {
            client.pods()
                    .inNamespace(namespace)
                    .withName(pod.getMetadata().getName())
                    .inContainer(containerName)
                    .dir("/vault/secrets")
                    .copy(tempDir);
        }

        private Pod findRunningPodOnEnvironment(KubernetesClient client) {
            PodList pods = client.pods().inNamespace(namespace).list();
            return pods.getItems().stream()
                    .filter(pod -> labelContainsName(pod.getMetadata().getLabels(), applicationName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Pod not found"));
        }

        private boolean labelContainsName(Map<String, String> labels, String name) {
            return name.equals(labels.get("name"));
        }

    }

    static class CassandraPropertiesPostProcessor extends TeamStarterPostProcessor<CassandraProperties> {

        public CassandraPropertiesPostProcessor(TeamStarterProperties properties, Path tempDir) {
            super(CassandraProperties.class, properties, tempDir);
        }

        public Object safelyPostProcessAfterInitialization(CassandraProperties bean, String beanName) {
            try (KubernetesClient client = getForClusterAndEnv()) {
                bean.getContactPoints().clear();
                bean.setSsl(false);
                bean.getContactPoints().add(getCassandraContactPointsFromConfigMap(client));
            }
            return bean;
        }
    }

    static class KafkaPropertiesPostProcessor extends TeamStarterPostProcessor<KafkaProperties> {

        public KafkaPropertiesPostProcessor(TeamStarterProperties properties, Path tempDir) {
            super(KafkaProperties.class, properties, tempDir);
        }

        public Object safelyPostProcessAfterInitialization(KafkaProperties bean, String beanName) {
            try (KubernetesClient client = getForClusterAndEnv()) {
                List<String> servers = getKafkaBootStrapServersFromConfigMap(client);
                bean.setBootstrapServers(servers);
            }
            return bean;
        }
    }

    static class YoltCassandraPropertiesPostProcessor extends TeamStarterPostProcessor<YoltCassandraProperties> {

        public YoltCassandraPropertiesPostProcessor(TeamStarterProperties properties, Path tempDir) {
            super(YoltCassandraProperties.class, properties, tempDir);
        }

        public Object safelyPostProcessAfterInitialization(YoltCassandraProperties bean, String beanName) {
            try (KubernetesClient client = getForClusterAndEnv()) {
                bean.getContactPoints().clear();
                bean.setSsl(false);
                bean.getContactPoints().add(getCassandraContactPointsFromConfigMap(client));
            }
            return bean;
        }
    }

    static class YoltVaultCassandraPropertiesPostProcessor extends TeamStarterPostProcessor<YoltVaultCassandraProperties> {

        private final Path tempDir;

        public YoltVaultCassandraPropertiesPostProcessor(TeamStarterProperties properties, Path tempDir) {
            super(YoltVaultCassandraProperties.class, properties, tempDir);
            this.tempDir = tempDir;
        }

        public Object safelyPostProcessAfterInitialization(YoltVaultCassandraProperties bean, String beanName) {
            bean.setVaultCredsFile(tempDir.resolve("vault/secrets/cassandra"));
            return bean;
        }
    }

    static class YoltVaultSpringCassandraPropertiesPostProcessor extends TeamStarterPostProcessor<nl.ing.lovebird.cassandra.spring.autoconfigure.YoltVaultCassandraProperties> {

        private final Path tempDir;

        public YoltVaultSpringCassandraPropertiesPostProcessor(TeamStarterProperties properties, Path tempDir) {
            super(nl.ing.lovebird.cassandra.spring.autoconfigure.YoltVaultCassandraProperties.class, properties, tempDir);
            this.tempDir = tempDir;
        }

        public Object safelyPostProcessAfterInitialization(nl.ing.lovebird.cassandra.spring.autoconfigure.YoltVaultCassandraProperties bean, String beanName) {
            bean.setVaultCredsFile(tempDir.resolve("vault/secrets/cassandra"));
            return bean;
        }
    }

    static class YoltVaultPostgresPropertiesPostProcessor extends TeamStarterPostProcessor<YoltVaultPostgresProperties> {

        private final Path tempDir;

        public YoltVaultPostgresPropertiesPostProcessor(TeamStarterProperties properties, Path tempDir) {
            super(YoltVaultPostgresProperties.class, properties, tempDir);
            this.tempDir = tempDir;
        }

        public Object safelyPostProcessAfterInitialization(YoltVaultPostgresProperties bean, String beanName) {
            bean.setVaultCredsFile(tempDir.resolve("vault/secrets/rds"));
            return bean;
        }
    }

    static class YoltVaultPropertiesPostProcessor extends TeamStarterPostProcessor<YoltVaultProperties> {

        private final Path tempDir;

        public YoltVaultPropertiesPostProcessor(TeamStarterProperties properties, Path tempDir) {
            super(YoltVaultProperties.class, properties, tempDir);
            this.tempDir = tempDir;
        }

        public Object safelyPostProcessAfterInitialization(YoltVaultProperties bean, String beanName) {
            bean.getSecrets().setDirectory(tempDir.resolve("vault/secrets").toString());
            bean.getSecret().setLocation(new FileSystemResource(tempDir.resolve("vault/secrets")));
            return bean;
        }
    }

    static class YoltVaultSecretsPropertiesPostProcessor extends TeamStarterPostProcessor<YoltVaultSecretsProperties> {

        private final Path tempDir;

        public YoltVaultSecretsPropertiesPostProcessor(TeamStarterProperties properties, Path tempDir) {
            super(YoltVaultSecretsProperties.class, properties, tempDir);
            this.tempDir = tempDir;
        }

        public Object safelyPostProcessAfterInitialization(YoltVaultSecretsProperties bean, String beanName) {
            bean.setDirectory(tempDir.resolve("vault/secrets").toString());
            return bean;
        }
    }
}

