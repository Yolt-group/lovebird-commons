package nl.ing.lovebird.cassandra.autoconfigure;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.Authenticator;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.EndPoint;
import com.datastax.driver.core.ExtendedAuthProvider;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SocketOptions;
import io.opentracing.Tracer;
import io.opentracing.contrib.cassandra.TracingCluster;
import io.opentracing.contrib.cassandra.nameprovider.QueryMethodTableSpanName;
import io.opentracing.contrib.cassandra.nameprovider.QuerySpanNameProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.cassandra.modelmutation.CassandraModelMutationApplier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import static java.util.Objects.requireNonNull;
import static nl.ing.lovebird.vault.Vault.requireFileProvidedByVault;

/**
 * {@link org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration} does not instantiate
 * the LoadBalancingPolicies or ReconnectionPolicies or RetryPolicies correctly since its trying to create an instance
 * from the NoArgs constructor which is private or does not exists.
 *
 * @author Algin Maduro
 */
@ConditionalOnClass({Cluster.class})
@AutoConfiguration
@EnableConfigurationProperties({YoltCassandraProperties.class, YoltVaultCassandraProperties.class})
@Slf4j
public class YoltCassandra3AutoConfiguration {

    private final YoltCassandraProperties cassandraProperties;
    private final YoltVaultCassandraProperties vaultProperties;

    public YoltCassandra3AutoConfiguration(YoltCassandraProperties cassandraProperties, YoltVaultCassandraProperties vaultProperties) {
        this.vaultProperties = vaultProperties;
        this.cassandraProperties = cassandraProperties;

        if (cassandraProperties.getKeyspaceName() == null || cassandraProperties.getKeyspaceName().isEmpty()) {
            throw new IllegalArgumentException("Please set the keyspace name for the keyspace you're trying to connect to, via the property 'spring.data.cassandra.keyspace-name'");
        }
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "yolt.vault.enabled", havingValue = "true")
    public AuthProvider vaultCassandraAuthProvider() {
        log.info("Using Cassandra credentials provided by vault-injector");
        requireFileProvidedByVault(vaultProperties.getVaultCredsFile());
        return new VaultCassandraCredentialsAuthProvider(vaultProperties.getVaultCredsFile());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "yolt.vault.enabled", havingValue = "false", matchIfMissing = true)
    public AuthProvider propertiesCassandraAuthProvider() {
        log.info("Using Cassandra credentials provided by properties");
        return new PlainTextAuthProvider(cassandraProperties.getUsername(), cassandraProperties.getPassword());
    }

    @Bean
    @ConditionalOnProperty(name = "yolt.commons.cassandra.tracing.enabled", havingValue = "false", matchIfMissing = true)
    @ConditionalOnMissingBean(Cluster.class)
    public Cluster cluster(final AuthProvider cassandraAuthProvider) {
        return clusterInitializer(cassandraAuthProvider).build();
    }

    @Bean
    @ConditionalOnProperty("yolt.commons.cassandra.tracing.enabled")
    @ConditionalOnMissingBean(Cluster.class)
    public Cluster tracingCluster(final Tracer tracer, final AuthProvider cassandraAuthProvider) {
        // Has to be retrieved this way, otherwise you get an error if it's not on the classpath
        QuerySpanNameProvider spanNameProvider = QueryMethodTableSpanName.newBuilder().build();
        return new TracingCluster(clusterInitializer(cassandraAuthProvider), tracer, spanNameProvider);
    }

    @Bean
    @ConditionalOnMissingBean(Cluster.Builder.class)
    public Cluster.Builder clusterInitializer(final AuthProvider cassandraAuthProvider) {
        Cluster.Builder builder = Cluster.builder()
                .withoutJMXReporting()
                .withClusterName(cassandraProperties.getClusterName())
                .withPort(cassandraProperties.getPort())
                .withQueryOptions(getQueryOptions())
                .withSocketOptions(getSocketOptions())
                .addContactPoints(cassandraProperties.getContactPoints().toArray(new String[0]))
                .withAuthProvider(cassandraAuthProvider);

        if (cassandraProperties.getCompression() != null) {
            builder.withCompression(cassandraProperties.getCompression());
        }

        if (cassandraProperties.isSsl()) {
            builder.withSSL();
        }

        return builder;
    }

    @Bean
    @ConditionalOnMissingBean(CassandraModelMutationApplier.class)
    public CassandraModelMutationApplier noopCassandraModelMutationApplier() {
        return () -> { /* noop */ };
    }

    /**
     * Require existence of the {@link CassandraModelMutationApplier} before creating a session.
     * <p>
     * This is done to make sure that if we're using the cassandra model mutation applier,
     * we wait for the applier to finish before initiating all the repositories.
     * Otherwise, a repository can reference a not-(yet)existing table, causing
     * it to fail.
     * <p>
     * So now we pick one of these:
     * 1. When in production use the No-op CassandraModelMutationApplier.
     * 2. When testing use the applier from the Cassandra3VersioningApplierAutoConfiguration
     */
    @Bean
    @ConditionalOnMissingBean(value = Session.class)
    public Session session(Cluster cluster, CassandraModelMutationApplier cassandraModelMutationApplier) {
        // The existence of the versioner applier ensures the name space exists.
        requireNonNull(cassandraModelMutationApplier);
        if (cassandraProperties.getKeyspaceName() != null) {
            return cluster.connect(cassandraProperties.getKeyspaceName());
        }

        return cluster.connect();
    }

    private QueryOptions getQueryOptions() {

        QueryOptions options = new QueryOptions();

        if (cassandraProperties.getConsistencyLevel() != null) {
            throw new UnsupportedOperationException("Please set your consistency level at either repository or query-level.");
        }

        if (cassandraProperties.getSerialConsistencyLevel() != null) {
            options.setSerialConsistencyLevel(cassandraProperties.getSerialConsistencyLevel());
        }

        options.setFetchSize(cassandraProperties.getFetchSize());

        return options;
    }

    private SocketOptions getSocketOptions() {
        final SocketOptions socketOptions = new SocketOptions();

        Optional.ofNullable(cassandraProperties.getConnectTimeout())
                .map(Duration::toMillis)
                .map(l -> (int) l.longValue())
                .ifPresent(socketOptions::setConnectTimeoutMillis);
        Optional.ofNullable(cassandraProperties.getReadTimeout())
                .map(Duration::toMillis)
                .map(l -> (int) l.longValue())
                .ifPresent(socketOptions::setReadTimeoutMillis);

        return socketOptions;
    }

    @RequiredArgsConstructor
    private static class VaultCassandraCredentialsAuthProvider implements ExtendedAuthProvider {
        private final Path vaultCredentialFile;
        private String username;

        private static Properties readCredentials(Path credentialsFilePath) {
            try (BufferedReader fileReader = Files.newBufferedReader(credentialsFilePath)) {
                Properties properties = new Properties();
                properties.load(fileReader);
                return properties;
            } catch (IOException e) {
                throw new IllegalStateException("Unable to load secrets from file:" + credentialsFilePath);
            }
        }

        @Override
        public Authenticator newAuthenticator(EndPoint endPoint, String authenticator) {
            final Properties credentials = readCredentials(vaultCredentialFile);
            String newUsername = (String) credentials.get("username");
            if (!Objects.equals(username, newUsername)) {
                log.info("Vault-injector provided a new username");
                username = newUsername;
            }
            String password = (String) credentials.get("password");
            PlainTextAuthProvider au = new PlainTextAuthProvider(newUsername, password);
            return au.newAuthenticator(endPoint, authenticator);
        }

        @Override
        public Authenticator newAuthenticator(InetSocketAddress host, String authenticator) {
            throw new AssertionError(
                    "The driver should never call this method on an object that implements "
                            + this.getClass().getSimpleName());
        }
    }
}
