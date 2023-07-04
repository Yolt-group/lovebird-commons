package nl.ing.lovebird.cassandra.test;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import lombok.experimental.UtilityClass;

import java.net.InetAddress;

/**
 * A Cassandra cluster for testing.
 *
 * The cluster may either connect to a test container or when running
 * in Gitlab a cassandra service.
 */
@UtilityClass
public class TestCassandraCluster {

    private static boolean isRunningInGitlabCI() {
        return System.getenv("GITLAB_CI") != null;
    }

    public static Cluster provideCluster() {
        if (isRunningInGitlabCI()) {
            return getClusterForGitlab();
        } else {
            return getClusterForTestContainers();
        }
    }

    public static int getClusterPort() {
        if (isRunningInGitlabCI()) {
            return getClusterForGitlab().getConfiguration().getProtocolOptions().getPort();
        } else {
            return CassandraTestContainerSingleton.instance().getFirstMappedPort();
        }
    }

    public static String getClusterHost() {
        if (isRunningInGitlabCI()) {
            return getClusterForGitlab().getMetadata()
                    .getAllHosts()
                    .stream()
                    .findAny()
                    .map(Host::getListenAddress)
                    .map(InetAddress::getHostName)
                    .orElseThrow(() -> new IllegalStateException("No cassandra hosts?"));
        } else {
            return CassandraTestContainerSingleton.instance().getHost();
        }
    }

    private static Cluster getClusterForGitlab() {
        return Cluster.builder()
                .withoutJMXReporting()
                .addContactPoint("localhost")
                .build();
    }

    private static Cluster getClusterForTestContainers() {
        return CassandraTestContainerSingleton.instance().getCluster();
    }

}
