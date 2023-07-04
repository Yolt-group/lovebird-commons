package nl.ing.lovebird.cassandra;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Update;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.annotations.Table;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import nl.ing.lovebird.cassandra.CassandraRepository.Tuple2;
import nl.ing.lovebird.logging.AuditLogger;
import nl.ing.lovebird.logging.test.CaptureLogEvents;
import nl.ing.lovebird.logging.test.LogEvents;
import nl.ing.lovebird.testsupport.cassandra.AbstractCassandraTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.set;
import static com.datastax.driver.mapping.Mapper.Option.consistencyLevel;
import static com.datastax.driver.mapping.Mapper.Option.tracing;
import static com.datastax.driver.mapping.Mapper.Option.ttl;
import static junit.framework.TestCase.assertFalse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * We're testing `nl.ing.lovebird:cassandra` here to avoid a circular
 * dependency on `nl.ing.lovebird:testsupport-cassandra`
 */
@CaptureLogEvents
class CassandraRepositoryIntegrationTest implements AbstractCassandraTest {

    private static final UUID USER_ID_0 = new UUID(0, 0);
    private static final UUID USER_ID_1 = new UUID(0, 1);
    private static final UUID USER_ID_2 = new UUID(0, 2);

    private Session session;
    private Session sessionUnboundToKeyspace;

    private LogEvents events;

    @BeforeEach
    void setup(LogEvents events) {
        this.events = events;
        this.session = Mockito.spy(MY_KEYSPACE_SESSION);
        this.sessionUnboundToKeyspace = MY_KEYSPACE_SESSION.getCluster().connect();

        Table table = User.class.getAnnotation(Table.class);

        String cql = String.format("TRUNCATE TABLE %s", table.name());
        session.execute(cql);

        Mockito.reset(session);
    }

    @Test
    void testConstructor() {
        assertDoesNotThrow(() -> new UserRepository(session));
    }

    @Test
    void doesNotAuditLogWhenDisabled() {
        UserRepository userRepository = new UserRepository(session);
        userRepository.setAuditLoggingEnabled(false);
        userRepository.auditLogSuccess("test", "entity");
        assertNoAuditSuccessCallMarker();
    }

    @Test
    void doesNotAuditLogErrorsWhenDisabled() {
        UserRepository userRepository = new UserRepository(session);
        userRepository.setAuditLoggingEnabled(false);
        userRepository.auditLogError("test", "entity", new Exception());
        assertNoAuditErrorCallMarker();
    }

    @Test
    void testConstructorNoTable() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new BadRepositoryNoTable(session));
        assertThat(e).hasMessage("java.lang.String does not have the com.datastax.driver.mapping.annotations.Table annotation.");
    }

    @Test
    void testConstructorUnsupportedKeyspace() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new BadRepositoryUnsupportedKeyspace(session));
        assertThat(e).hasMessage("@Table.keyspace is not supported anymore since we need to be able to dynamically set the keyspace. See Changelog lovebird commons for instructions");
    }

    @Test
    void testInsertCountDelete() {
        UserRepository repository = new UserRepository(session);
        User user0 = new User(USER_ID_0, "Roger");
        User user1 = new User(USER_ID_1, "someName");
        User user2 = new User(USER_ID_2, null);

        assertThat(repository.count()).isZero();
        repository.save(user0);
        repository.save(Arrays.asList(user1, user2));

        assertThat(repository.count()).isEqualTo(3);

        assertThat(repository.selectOne(user0.getUserId())).hasValue(user0);

        repository.delete(Arrays.asList(user0, user1, user2));

        assertThat(repository.count()).isZero();
    }

    @Test
    void testDeleteFailure() {
        UserRepository repository = new UserRepository(session);
        User user = new User(USER_ID_0, "Roger");
        repository.save(user);
        assertThat(repository.count()).isEqualTo(1);

        assertThrows(Exception.class, () -> repository.delete(new User(null, null)));

        assertThat(repository.count()).isEqualTo(1);
        assertAuditErrorCallMarker();
    }

    @Test
    void testAuditLoggingSuccessOnSave() {
        UserRepository repository = new UserRepository(session);
        User user = new User(USER_ID_0, null);
        repository.save(user);
        assertThat(repository.count()).isEqualTo(1);
        assertAuditSuccessCallMarker();
    }

    @Test
    void testAuditLoggingFailureOnSave() {
        UserRepository repository = new UserRepository(session);
        User user = new User();
        user.setUserId(null);

        assertThrows(Exception.class, () -> repository.save(user));

        assertAuditErrorCallMarker();
    }

    @Test
    void testBatchSave() {
        UserRepository repository = new UserRepository(session);
        User user0 = new User(USER_ID_0, "Roger");
        User user1 = new User(USER_ID_1, "someName");
        User user2 = new User(USER_ID_2, null);

        assertThat(repository.count()).isZero();

        Mockito.reset(session);
        repository.saveBatch(Arrays.asList(user0, user1, user2), 1000);

        verify(session).execute(any(Statement.class));

        assertThat(repository.count()).isEqualTo(3);
    }

    @Test
    void testBatchSaveWithIndividualOptions() {
        UserRepository repository = new UserRepository(session);

        Tuple2<User, ImmutableList<Mapper.Option>> user0
                = new Tuple2<>(new User(USER_ID_0, "Robin"), ImmutableList.of(ttl(123), tracing(true), consistencyLevel(ConsistencyLevel.LOCAL_ONE)));
        Tuple2<User, ImmutableList<Mapper.Option>> user1
                = new Tuple2<>(new User(USER_ID_1, "Bassie"), ImmutableList.of(ttl(456), tracing(false), consistencyLevel(ConsistencyLevel.LOCAL_QUORUM)));
        Tuple2<User, ImmutableList<Mapper.Option>> user2
                = new Tuple2<>(new User(USER_ID_2, "Adriaan"), ImmutableList.of(ttl(789), tracing(true), consistencyLevel(ConsistencyLevel.LOCAL_SERIAL)));

        assertThat(repository.count()).isZero();

        Mockito.reset(session);
        repository.saveBatchWithOption(ImmutableList.of(user0, user1, user2), 1000);

        ArgumentCaptor<BatchStatement> argument = ArgumentCaptor.forClass(BatchStatement.class);
        verify(session).execute(argument.capture());
        assertThat(argument.getValue().getStatements().size()).isEqualTo(3);

        ArrayList<Statement> statements = Lists.newArrayList(argument.getValue().getStatements());
        assertThat(statements.get(0).getConsistencyLevel()).isEqualTo(ConsistencyLevel.LOCAL_ONE);
        assertThat(statements.get(1).getConsistencyLevel()).isEqualTo(ConsistencyLevel.LOCAL_QUORUM);
        assertThat(statements.get(2).getConsistencyLevel()).isEqualTo(ConsistencyLevel.LOCAL_SERIAL);
        assertThat(statements.get(0).isTracing()).isEqualTo(true);
        assertThat(statements.get(1).isTracing()).isEqualTo(false);
        assertThat(statements.get(2).isTracing()).isEqualTo(true);
        assertThat(repository.count()).isEqualTo(3);
        assertThat(getUserRow(session, USER_ID_0).getInt("TTL(name)")).isBetween(118, 123);
        assertThat(getUserRow(session, USER_ID_1).getInt("TTL(name)")).isBetween(451, 456);
        assertThat(getUserRow(session, USER_ID_2).getInt("TTL(name)")).isBetween(784, 789);
    }

    private Row getUserRow(final Session session, final UUID userId) {
        Table table = User.class.getAnnotation(Table.class);
        return session.execute(
                new BoundStatement(session.prepare("SELECT name, TTL(name) from " + table.name() + " where user_id = " + userId.toString()))).one();
    }

    @Test
    void testBatchSaveWithSplit() {
        UserRepository repository = new UserRepository(session);
        User user0 = new User(USER_ID_0, "Roger");
        User user1 = new User(USER_ID_1, "someName");
        User user2 = new User(USER_ID_2, null);

        assertThat(repository.count()).isZero();

        Mockito.reset(session);
        repository.saveBatch(Arrays.asList(user0, user1, user2), 1);

        verify(session, times(3)).execute(any(Statement.class));

        assertThat(repository.count()).isEqualTo(3);
    }

    @Test
    void testAuditLoggingSuccessOnBatchSave() {
        UserRepository repository = new UserRepository(session);
        User user = new User(USER_ID_0, null);
        repository.saveBatch(Collections.singletonList(user), 1000);
        assertThat(repository.count()).isEqualTo(1);
        assertAuditSuccessCallMarker();
    }

    @Test
    void testAuditLoggingFailureOnBatchSave() {
        UserRepository repository = new UserRepository(session);
        User user = new User();
        user.setUserId(null);

        assertThrows(Exception.class, () -> repository.saveBatch(Collections.singletonList(user), 1000));

        assertAuditErrorCallMarker();
    }

    @Test
    void testAuditLoggingInsertSuccess() {
        UserRepository repository = new UserRepository(session);
        Insert insert = repository.createInsert();
        insert.value(User.USER_ID_COLUMN, USER_ID_0);
        insert.value(User.NAME_COLUMN, null);

        ResultSet resultSet = repository.executeInsert(insert);

        assertThat(resultSet.wasApplied()).isEqualTo(true);
        assertThat(repository.count()).isEqualTo(1);
        assertAuditSuccessCallMarker();
    }

    @Test
    void testAuditLoggingInsertFailure() {
        UserRepository repository = new UserRepository(session);
        Insert insert = repository.createInsert();
        insert.value(User.USER_ID_COLUMN, null);
        insert.value(User.NAME_COLUMN, null);

        assertThrows(Exception.class, () -> repository.executeInsert(insert));
        assertAuditErrorCallMarker();
    }

    @Test
    void testExecuteUpdate() {
        final String nameAfterUpdate = "nameAfterUpdate";

        UserRepository repository = new UserRepository(session);

        User user0 = new User(USER_ID_0, "name");
        repository.save(user0);
        user0.setName(nameAfterUpdate);


        final Update updateQuery = QueryBuilder.update(User.TABLE_NAME);
        Update.Assignments assignments = updateQuery.with(set(User.NAME_COLUMN, nameAfterUpdate));
        updateQuery.where(eq(User.USER_ID_COLUMN, USER_ID_0));

        repository.executeUpdate(updateQuery);

        User retrievedUser = repository.selectOne(USER_ID_0).get();

        assertThat(repository.selectOne(user0.getUserId())).hasValue(user0);
        assertEquals(nameAfterUpdate, retrievedUser.getName());
        assertAuditSuccessCallMarker();
    }

    @Test
    void testExecuteUpdateFailure() {
        UserRepository repository = new UserRepository(session);

        User user0 = new User(USER_ID_0, null);

        repository.save(user0);

        final Update updateQuery = QueryBuilder.update(User.TABLE_NAME);
        Update.Assignments assignments = updateQuery.with(set(User.NAME_COLUMN, null));

        assertThrows(Exception.class, () -> repository.executeUpdate(updateQuery));

        assertAuditErrorCallMarker();
    }

    @Test
    void testExecuteDelete() {
        UserRepository repository = new UserRepository(session);
        User user0 = new User(USER_ID_0, "name");
        repository.save(user0);
        assertThat(repository.selectOne(user0.getUserId())).hasValue(user0);

        Delete delete = QueryBuilder.delete().from(User.TABLE_NAME);
        delete.where(eq(User.USER_ID_COLUMN, USER_ID_0));
        repository.executeDelete(delete);

        Optional<User> retrievedUser = repository.selectOne(USER_ID_0);
        assertFalse(retrievedUser.isPresent());
        assertAuditSuccessCallMarker();
    }

    @Test
    void testExecuteDeleteFailure() {
        UserRepository repository = new UserRepository(session);
        User user0 = new User(USER_ID_0, "name");
        repository.save(user0);
        assertThat(repository.selectOne(user0.getUserId())).hasValue(user0);

        Delete delete = QueryBuilder.delete().from(User.TABLE_NAME);

        assertThrows(Exception.class, () -> repository.executeDelete(delete));
        Optional<User> retrievedUser = repository.selectOne(USER_ID_0);
        assertTrue(retrievedUser.isPresent());
        assertAuditErrorCallMarker();
    }

    @Test
    void testDefaultConsistencyCount() {
        UserRepository repository = new UserRepository(session);
        repository.count();

        ArgumentCaptor<Statement> captor = ArgumentCaptor.forClass(Statement.class);

        verify(session).execute(captor.capture());

        assertThat(captor.getValue()).isInstanceOf(SimpleStatement.class);
        assertThat(captor.getValue().getConsistencyLevel()).isEqualTo(ConsistencyLevel.LOCAL_QUORUM);
    }

    @Test
    void testDefaultConsistencySave() {
        UserRepository repository = new UserRepository(session);
        final Delete deleteQuery = QueryBuilder.delete().from(User.TABLE_NAME);
        deleteQuery.where(eq(User.USER_ID_COLUMN, UUID.randomUUID()));
        repository.executeDelete(deleteQuery);

        ArgumentCaptor<Statement> captor = ArgumentCaptor.forClass(Statement.class);

        verify(session).execute(captor.capture());

        assertThat(captor.getValue()).isInstanceOf(Delete.class);
        assertThat(captor.getValue().getConsistencyLevel()).isEqualTo(ConsistencyLevel.LOCAL_QUORUM);
    }

    @Test
    void testSpecificConsistencyCount() {
        UserRepository repository = new UserRepository(session, ConsistencyLevel.LOCAL_QUORUM, ConsistencyLevel.QUORUM);
        repository.count();

        ArgumentCaptor<Statement> captor = ArgumentCaptor.forClass(Statement.class);

        verify(session).execute(captor.capture());

        assertThat(captor.getValue()).isInstanceOf(SimpleStatement.class);
        assertThat(captor.getValue().getConsistencyLevel()).isEqualTo(ConsistencyLevel.LOCAL_QUORUM);
    }

    @Test
    void testSaveWithTTL() throws Exception {
        UserRepository repository = new UserRepository(session);

        User user0 = new User(USER_ID_0, "Name");

        repository.save(user0, ttl(3));
        assertThat(repository.count()).isEqualTo(1);

        Thread.sleep(5_000);

        assertThat(repository.count()).isZero();
    }

    @Test
    void testBeingAbleToQueryMultipleKeyspaces() {
        UUID userId = UUID.fromString("68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0");


        UserRepository repositoryMyKespace = new UserRepository(sessionUnboundToKeyspace, session.getLoggedKeyspace());
        UserRepository repositoryOtherSchema = new UserRepository(sessionUnboundToKeyspace, BadUserClass.OTHER_KEYSPACE);

        repositoryMyKespace.save(new User(userId, "user in my_keyspace"));
        repositoryOtherSchema.save(new User(userId, "user in " + BadUserClass.OTHER_KEYSPACE));

        Optional<User> user = repositoryMyKespace.selectOne(userId);
        Optional<User> userOtherKeyspace = repositoryOtherSchema.selectOne(userId);

        assertNotEquals(user, userOtherKeyspace);
        assertThat(user.get().getName()).contains("my_keyspace");
        assertThat(userOtherKeyspace.get().getName()).contains(BadUserClass.OTHER_KEYSPACE);

        UUID user2Id = UUID.randomUUID();
        repositoryOtherSchema.save(new User(user2Id, "user 2"));
        repositoryOtherSchema.save(new User(UUID.randomUUID(), "user 3"));
        assertThat(repositoryOtherSchema.count()).isEqualTo(3);

        repositoryOtherSchema.delete(new User(user2Id, "user 2"));
        assertThat(repositoryOtherSchema.count()).isEqualTo(2);

    }

    private void assertAuditSuccessCallMarker() {
        ILoggingEvent event = events.stream(AuditLogger.class, Level.INFO).findFirst().get();
        assertThat(event.getMarker().toString()).contains("result=SUCCESS");
    }
    private void assertNoAuditSuccessCallMarker() {
        Optional<ILoggingEvent> event = events.stream(AuditLogger.class, Level.INFO).findFirst();
        assertThat(event).isEmpty();
    }

    private void assertAuditErrorCallMarker() {
        ILoggingEvent event = events.stream(AuditLogger.class, Level.ERROR).findFirst().get();
        assertThat(event.getMarker().toString()).contains("result=ERROR");
    }

    private void assertNoAuditErrorCallMarker() {
        Optional<ILoggingEvent> event = events.stream(AuditLogger.class, Level.ERROR).findFirst();
        assertThat(event).isEmpty();
    }

    public static class BadRepositoryNoTable extends CassandraRepository<String> {
        protected BadRepositoryNoTable(Session session) {
            super(session, String.class);
        }
    }

    public static class BadRepositoryUnsupportedKeyspace extends CassandraRepository<BadUserClass> {
        protected BadRepositoryUnsupportedKeyspace(Session session) {
            super(session, BadUserClass.class);
        }
    }
}
