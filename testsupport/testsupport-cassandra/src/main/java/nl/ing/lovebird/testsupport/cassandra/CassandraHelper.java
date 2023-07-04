package nl.ing.lovebird.testsupport.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import com.datastax.driver.mapping.annotations.Table;
import nl.ing.lovebird.cassandra.CassandraRepository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * Use a base repository test like in the transactions service instead. This uses the cassandraUpdates versioning scripts
 */
public class CassandraHelper {
    private static final String EXCEPTION_MESSAGE_TABLE_ANNOTATION_KEYSPACE_UNSUPPORTED = "Unable to determine the keyspace. " +
            "@Table.keyspace is not supported anymore. " +
            "Provide the keyspace on the session, or use a method with keyspace as input parameter.";

    private CassandraHelper(){

    }

    public static long count(final Session session, final Class<?> clazz, String where) {
        if (!StringUtils.hasLength(session.getLoggedKeyspace())) {
            throw new IllegalArgumentException(EXCEPTION_MESSAGE_TABLE_ANNOTATION_KEYSPACE_UNSUPPORTED);
        }

        String query = String.format("SELECT COUNT(*) FROM %s.%s WHERE %s", session.getLoggedKeyspace(), getAnnotation(clazz).name(), where);

        return count(session, query);
    }

    public static long count(final Session session, final Class<?> clazz) {
        if (!StringUtils.hasLength(session.getLoggedKeyspace())) {
            throw new IllegalArgumentException(EXCEPTION_MESSAGE_TABLE_ANNOTATION_KEYSPACE_UNSUPPORTED);
        }

        String query = String.format("SELECT COUNT(*) FROM %s.%s", session.getLoggedKeyspace(), getAnnotation(clazz).name());

        return count(session, query);
    }

    public static long count(final Session session, String query) {
        return session.execute(query).one().getLong(0);
    }

    private static Table getAnnotation(final Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            throw new IllegalArgumentException(String.format("%s is not a Cassandra domain object", clazz.getTypeName()));
        }

        return table;
    }

    public static String getTableName(final Class<?> clazz) {
        return getAnnotation(clazz).name();
    }

    /**
     * @deprecated We run all all model mutation .cql files at application startup. Truncating tables will
     * remove this data. This means that one test will have the data, the next won't. This results in flakey tests.
     *
     * Prefer creating users/accounts/transactions/ect with random UUIDs instead.
     */
    @Deprecated
    public static void truncate(final Session session, final Class<?>... classes) {
        if (StringUtils.isEmpty(session.getLoggedKeyspace())) {
            throw new IllegalArgumentException(EXCEPTION_MESSAGE_TABLE_ANNOTATION_KEYSPACE_UNSUPPORTED);
        }
        for (Class<?> c : classes) {
            String cql = String.format("TRUNCATE TABLE %s", getAnnotation(c).name());
            session.execute(cql);
        }
    }

    public static <T> OpenRepository<T> openRepository(Session session, Class<T> clazz) {
        return new OpenRepository<>(session, clazz);
    }

    public static <T> OpenRepository<T> openRepository(Session session, Class<T> clazz, String keyspaceOverride) {
        return new OpenRepository<>(session, clazz, keyspaceOverride);
    }

    /**
     * Exposes all the protected method in the base CassandraRepository class for use in Integration Tests
     */
    public static class OpenRepository<T> extends CassandraRepository<T> {
        private OpenRepository(Session session, Class<T> clazz) {
            super(session, clazz);
        }

        private OpenRepository(Session session, Class<T> clazz, String keyspaceOverride) {
            super(session, clazz, keyspaceOverride);
        }

        public long count() {
            return super.count();
        }

        public void save(T entity) {
            super.save(entity);
        }

        public void save(List<T> entities) {
            super.save(entities);
        }

        public long count(Statement select) {
            return super.count(select);
        }

        public long count(String select) {
            return super.count(new SimpleStatement(select));
        }

        public List<T> select(Select select) {
            return super.select(select);
        }

        public List<T> selectAll() {
            SimpleStatement statement = new SimpleStatement(String.format("SELECT * FROM %s.%s", keyspace, table));

            return super.select(statement);
        }

        public Optional<T> selectOne(Select select) {
            return super.selectOne(select);
        }

        public void delete(T entity) {
            super.delete(entity);
        }

        public void delete(List<T> entities) {
            super.delete(entities);
        }

        public void executeUpdate(final Update update, final T entity) {
            super.executeUpdate(update);
        }

        public ResultSet executeDelete(final Delete delete) {
            return super.executeDelete(delete);
        }

    }
}
