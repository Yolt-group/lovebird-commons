package nl.ing.lovebird.cassandra;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import com.datastax.driver.extras.codecs.enums.EnumNameCodec;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Mapper.Option;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Table;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.micrometer.core.annotation.Timed;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.logging.AuditLogger;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.datastax.driver.mapping.Mapper.Option.consistencyLevel;

/**
 * Basic Cassandra Repository. Homegrown and organic.
 * <p>
 * NOTE: The use of this class is discouraged. For new tables use Spring Data Cassandra instead.
 * <p>
 * This implementation has several problems:
 *
 * <ul>
 *     <li>The session is a closable bean. As a result the application context can not be reloaded.
 *     <li>The Cassandra v3 driver API is exposed and necessary to use the CassandraRepository.
 *          This makes upgrading to v4 as difficult as upgrading to Spring Data Cassandra.
 *     <li>The Yolt Cassandra integration and and Spring Data Cassandra integration depend on the
 *     same configuration tree. While they currently do not conflict, they may well in the future.
 * </ul>
 */
@Slf4j
@Timed("repository")
public class CassandraRepository<T> {

    static final String MY_POD_NAMESPACE_ENV_VARIABLE = "MY_POD_NAMESPACE";
    static final String MY_POD_NAMESPACE_DEFAULT_NAMESPACE = "default";
    /**
     * System keyspaces are not namespace-aware, so they would have the same name in any namespace
     */
    private static final List<String> SYSTEM_KEYSPACES = Arrays.asList("system", "system_auth", "system_distributed",
            "system_schema", "system_traces");
    protected final Session session;
    protected final Mapper<T> mapper;
    protected final String table;
    protected final String keyspace;
    protected final ConsistencyLevel writeConsistency;
    protected final ConsistencyLevel readConsistency;
    protected final Class<T> clazz;
    private final SimpleStatement countStatement;
    private boolean tracingEnabled;
    private boolean auditLoggingEnabled = true;

    protected CassandraRepository(final Session session, final Class<T> clazz) {
        this(session, clazz, ConsistencyLevel.LOCAL_QUORUM, ConsistencyLevel.LOCAL_QUORUM, null);
    }

    protected CassandraRepository(final Session session, final Class<T> clazz, String keyspaceOverride) {
        this(session, clazz, ConsistencyLevel.LOCAL_QUORUM, ConsistencyLevel.LOCAL_QUORUM, keyspaceOverride);
    }

    protected CassandraRepository(
            final Session session,
            final Class<T> clazz,
            final ConsistencyLevel readConsistency,
            final ConsistencyLevel writeConsistency) {
        this(session, clazz, readConsistency, writeConsistency, null);
    }

    protected CassandraRepository(
            final Session session,
            final Class<T> clazz,
            final ConsistencyLevel readConsistency,
            final ConsistencyLevel writeConsistency,
            final String keyspaceOverride) {

        if (clazz.getAnnotation(Table.class) == null) {
            throw new IllegalArgumentException(clazz.getName() + " does not have the " + Table.class.getName() + " annotation.");
        }

        if (!StringUtils.isEmpty(clazz.getAnnotation(Table.class).keyspace())) {
            throw new IllegalArgumentException("@Table.keyspace is not supported anymore since we need to be able to dynamically set the" +
                                               " keyspace. See Changelog lovebird commons for instructions");
        }

        this.session = session;
        this.clazz = clazz;
        this.readConsistency = readConsistency;
        this.writeConsistency = writeConsistency;
        this.table = clazz.getAnnotation(Table.class).name();

        if (!StringUtils.isEmpty(keyspaceOverride)) {
            keyspace = keyspaceOverride;
            mapper = new MappingManager(session).mapper(clazz, keyspaceOverride);
        } else {
            if (StringUtils.isEmpty(session.getLoggedKeyspace())) {
                throw new IllegalArgumentException("cannot resolve the keyspace for " + this.getClass().getName() +
                                                   " This should be configured on the session.");
            }
            keyspace = session.getLoggedKeyspace();
            mapper = new MappingManager(session).mapper(clazz);
        }

        if (readConsistency != null) {
            mapper.setDefaultGetOptions(withOptionalTracing(consistencyLevel(readConsistency)));
        }
        if (writeConsistency != null) {
            mapper.setDefaultDeleteOptions(withOptionalTracing(consistencyLevel(writeConsistency)));
            mapper.setDefaultSaveOptions(withOptionalTracing(consistencyLevel(writeConsistency)));
        }

        checkIsKeyspaceValid();

        countStatement = withOptionalTracing(new SimpleStatement(String.format("SELECT COUNT(*) FROM %s.%s", keyspace, table)));
    }

    public void setTracingEnabled(boolean tracingEnabled) {
        this.tracingEnabled = tracingEnabled;
    }

    private void checkIsKeyspaceValid() {
        String podNamespace = System.getenv(MY_POD_NAMESPACE_ENV_VARIABLE);

        if (StringUtils.isEmpty(podNamespace)) {
            return;
        }
        if (podNamespace.trim().equals(MY_POD_NAMESPACE_DEFAULT_NAMESPACE)) {
            return;
        }
        if (!keyspace.startsWith(podNamespace) && !isSystemKeyspace(keyspace)) {
            throw new IllegalArgumentException("Unable to create CassandraRepository. Keyspace should be prefixed with " + podNamespace);
        }
    }

    private boolean isSystemKeyspace(final String keyspace) {
        return SYSTEM_KEYSPACES.contains(keyspace);
    }

    protected void save(final T entity, Option... options) {
        try {
            mapper.save(entity, withOptionalTracing(options));
            String auditMessageSuccess = String.format("Saved %s", table);
            auditLogSuccess(auditMessageSuccess, entity);
        } catch (Throwable e) {
            String auditErrorMessage = String.format("Error while saving %s", table);
            auditLogError(auditErrorMessage, entity, e);
            throw e;
        }
    }

    protected void save(final T entity) {
        this.save(entity, new Option[0]);
    }

    protected void save(final List<T> entities) {
        entities.forEach(this::save);
    }

    /**
     * Using unlogged batch improves performance significantly in comparison with {@code entities.forEach(this::save)}
     * <p>
     * Performance improvements are apparent especially when all records in the batch are written to the same partition.
     * <p>
     * For large number of entities it is necessary to do writes in several batches. Batch size depends on total size
     * of the data in the batch. Many columns with long strings implies smaller batch size.
     * 1000 is a safe option for most use cases.
     *
     * @param entities     List of entities to save
     * @param maxBatchSize Maximum size of a single batch
     */
    public void saveBatch(final List<T> entities, final int maxBatchSize) {
        List<Tuple2<T, ImmutableList<Option>>> entitiesWithEmptyOption = entities.stream()
                .map(t -> new Tuple2<>(t, ImmutableList.<Option>of()))
                .collect(Collectors.toList());
        saveBatchWithOption(entitiesWithEmptyOption, maxBatchSize);
    }

    public void saveBatchWithOption(final List<Tuple2<T, ImmutableList<Option>>> entitiesWithOptions, final int maxBatchSize) {

        Lists.partition(entitiesWithOptions, maxBatchSize).forEach(subBatch -> {
            BatchStatement batch = new BatchStatement(BatchStatement.Type.UNLOGGED);
            batch.setConsistencyLevel(writeConsistency);
            if (tracingEnabled) {
                batch.enableTracing();
            }

            subBatch.forEach(t -> batch.add(mapper.saveQuery(t._1, t._2.toArray(new Option[0]))));

            try {
                String auditMessageSuccess = String.format("Saving a batch to %s", table);
                session.execute(batch);
                auditLogSuccess(auditMessageSuccess, subBatch);
            } catch (Throwable throwable) {
                String auditErrorMessage = String.format("Error while saving a batch to %s", table);
                auditLogError(auditErrorMessage, subBatch, throwable);
                throw throwable;
            }
        });
    }

    protected void delete(final T entity) {
        try {
            mapper.delete(entity);
            String auditMessageSuccess = String.format("Deleted %s", table);
            auditLogSuccess(auditMessageSuccess, entity);
        } catch (Throwable e) {
            String auditErrorMessage = String.format("Error while deleting %s", table);
            auditLogError(auditErrorMessage, entity, e);
            throw e;
        }
    }

    protected void delete(final List<T> entities) {
        entities.forEach(this::delete);
    }

    protected ResultSet executeUpdate(final Update update) {
        try {
            ResultSet resultSet = session.execute(withConsistencyLevel(withOptionalTracing(update), writeConsistency));
            String auditMessageSuccess = String.format("Update query executed : %s ", update.toString());
            auditLogSuccess(auditMessageSuccess, null);
            return resultSet;
        } catch (Throwable e) {
            String auditErrorMessage = String.format("Update query failed : %s", update.toString());
            auditLogError(auditErrorMessage, null, e);
            throw e;
        }
    }

    protected ResultSet executeDelete(final Delete delete) {
        try {
            ResultSet resultSet = session.execute(withConsistencyLevel(withOptionalTracing(delete), writeConsistency));
            String auditMessageSuccess = String.format("Delete query executed : %s ", delete.toString());
            auditLogSuccess(auditMessageSuccess, null);
            return resultSet;
        } catch (Throwable e) {
            String auditErrorMessage = String.format("Delete query failed : %s ", delete.toString());
            auditLogError(auditErrorMessage, null, e);
            throw e;
        }
    }

    protected ResultSet executeInsert(final Insert insert) {
        try {
            ResultSet resultSet = session.execute(withConsistencyLevel(withOptionalTracing(insert), writeConsistency));
            String auditMessageSuccess = String.format("Insert query executed : %s ", insert.toString());
            auditLogSuccess(auditMessageSuccess, null);
            return resultSet;
        } catch (Throwable e) {
            String auditErrorMessage = String.format("Insert query failed : %s ", insert.toString());
            auditLogError(auditErrorMessage, null, e);
            throw e;
        }
    }

    protected long count() {
        return count(countStatement);
    }

    protected long count(final Statement select) {
        ResultSet resultSet = session.execute(withConsistencyLevel(withOptionalTracing(select), readConsistency));

        return resultSet.one().getLong(0);
    }

    /**
     * Mind that this method is keyspace unaware, so you will have to set your keyspace when building the statement.
     * Only use this method when the select(Clause) method is insufficient.
     *
     * @param select The statement where the result will be mapped in a List of T.
     * @return The mapped objects of T as a result of the select statement.
     */
    protected List<T> select(final Statement select) {
        Result<T> result = mapper.map(session.execute(withConsistencyLevel(withOptionalTracing(select), readConsistency)));

        return result.all();
    }

    /**
     * Mind that this method is keyspace unaware, so you will have to set your keyspace when building the statement.
     * Only use this method when the selectOne(Clause) method is insufficient.
     *
     * @param select The statement where the result will be mapped to an Optional of T.
     * @return The mapped object of T inside of an Optional or an empty Optional as a result of the select statement.
     */
    protected Optional<T> selectOne(final Statement select) {
        ResultSet resultSet = session.execute(withConsistencyLevel(withOptionalTracing(select), readConsistency));
        Result<T> result = mapper.map(resultSet);

        return Optional.ofNullable(result.one());
    }

    protected List<T> select(final Clause clause) {
        Result<T> result = mapper.map(session.execute(withConsistencyLevel(withOptionalTracing(createSelect(clause)), readConsistency)));

        return result.all();
    }

    protected Optional<T> selectOne(final Clause clause) {
        ResultSet resultSet = session.execute(withConsistencyLevel(withOptionalTracing(createSelect(clause)), readConsistency));
        Result<T> result = mapper.map(resultSet);

        return Optional.ofNullable(result.one());
    }

    protected void delete(final Clause clause) {
        session.execute(withConsistencyLevel(withOptionalTracing(createDelete(clause)), writeConsistency));
    }

    protected Select createSelect() {
        return withConsistencyLevel(withOptionalTracing(QueryBuilder.select().from(keyspace, table)), readConsistency);
    }

    protected Select createSelect(final Clause clause) {
        Select select = withOptionalTracing(createSelect());
        select.where(clause);

        return select;
    }

    protected Delete createDelete() {
        return withConsistencyLevel(withOptionalTracing(QueryBuilder.delete().from(keyspace, table)), writeConsistency);
    }

    protected Delete createDelete(final Clause clause) {
        Delete delete = withOptionalTracing(createDelete());
        delete.where(clause);

        return delete;
    }

    protected Insert createInsert() {
        return withConsistencyLevel(withOptionalTracing(QueryBuilder.insertInto(keyspace, table)), writeConsistency);
    }

    protected Update createUpdate() {
        return withConsistencyLevel(withOptionalTracing(QueryBuilder.update(keyspace, table)), writeConsistency);
    }

    private <T extends Statement> T withConsistencyLevel(final T statement, final ConsistencyLevel level) {
        if (level != null && statement.getConsistencyLevel() == null) {
            statement.setConsistencyLevel(level);
        }

        return statement;
    }

    protected <E extends Enum<E>> void registerEnum(final Class<E> clazz) {
        session.getCluster().getConfiguration().getCodecRegistry().register(new EnumNameCodec<>(clazz));
    }

    private <R extends Statement> R withOptionalTracing(R statement) {
        return tracingEnabled ? (R) statement.enableTracing() : statement;
    }

    private Option[] withOptionalTracing(Option... options) {
        return tracingEnabled ? MapperOptionUtils.withTracing(options) : options;
    }

    protected void setAuditLoggingEnabled(boolean auditLoggingEnabled) {
        this.auditLoggingEnabled = auditLoggingEnabled;
    }

    protected <E> void auditLogError(String auditMessageError, E entity, Throwable e) {
        if (auditLoggingEnabled) {
            AuditLogger.logError(auditMessageError, entity, e);
        }
    }

    protected <E> void auditLogSuccess(String auditMessageSuccess, E entity) {
        if (auditLoggingEnabled) {
            AuditLogger.logSuccess(auditMessageSuccess, entity);
        }
    }

    @ToString
    @EqualsAndHashCode
    public static class Tuple2<X, Y> {
        public final X _1;
        public final Y _2;

        public Tuple2(X x, Y y) {
            this._1 = x;
            this._2 = y;
        }
    }
}
