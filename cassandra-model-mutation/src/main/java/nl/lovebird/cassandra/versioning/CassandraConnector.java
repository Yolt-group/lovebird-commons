package nl.lovebird.cassandra.versioning;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import java.io.Closeable;

class CassandraConnector implements Closeable {

    private Cluster cluster;

    private Session session;

    public void connect(Cluster.Builder clusterInitializer, String keyspace) {
        cluster = clusterInitializer
                .withoutJMXReporting()
                .build();
        session = cluster.connect(keyspace);
    }

    public Session getSession() {
        return this.session;
    }

    public void close() {
        if (session != null) {
            session.close();
        }
        if (cluster != null) {
            cluster.close();
        }
    }
}
