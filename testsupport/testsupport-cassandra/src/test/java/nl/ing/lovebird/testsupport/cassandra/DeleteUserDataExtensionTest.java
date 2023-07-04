package nl.ing.lovebird.testsupport.cassandra;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class DeleteUserDataExtensionTest implements AbstractCassandraTest {

    private static final UUID userId = UUID.randomUUID();
    private static final UUID accountId1 = UUID.randomUUID();
    private static final UUID accountId2 = UUID.randomUUID();

    private DeleteUserDataExtension deleteUserDataExtension;

    @BeforeEach
    void setup() {
        deleteUserDataExtension = new DeleteUserDataExtension();
        deleteUserDataExtension.setSession(TEST_KEYSPACE_SESSION);

        TEST_KEYSPACE_SESSION.getCluster().getMetadata().getKeyspace(TEST_KEYSPACE_SESSION.getLoggedKeyspace())
                .getTables().forEach(tableMetadata -> TEST_KEYSPACE_SESSION.execute("TRUNCATE TABLE " + tableMetadata.getName())
        );
    }

    @Test
    void shouldNotAssertOnProperDataDeletion() {
        deleteUserDataExtension.setUserId(userId);
        deleteUserDataExtension.setSession(TEST_KEYSPACE_SESSION);

        TEST_KEYSPACE_SESSION.execute("INSERT INTO one_key (user_id, number) VALUES (?, 1);", userId);
        TEST_KEYSPACE_SESSION.execute("INSERT INTO two_keys (user_id, account_id, number) VALUES (?, ?, 1);", userId, accountId1);
        TEST_KEYSPACE_SESSION.execute("INSERT INTO two_keys (user_id, account_id, number) VALUES (?, ?, 1);", userId, accountId2);

        Assertions.assertDoesNotThrow(() -> deleteUserDataExtension.beforeTestExecution(null));

        TEST_KEYSPACE_SESSION.execute("DELETE FROM one_key WHERE user_id = ?;", userId);
        TEST_KEYSPACE_SESSION.execute("DELETE FROM two_keys WHERE user_id = ?;", userId);

        Assertions.assertDoesNotThrow(() -> deleteUserDataExtension.afterTestExecution(null));
    }

    @Test
    void shouldNotAssertOnProperDataDeletionWithExcludedTable() {
        deleteUserDataExtension.setUserId(userId);
        deleteUserDataExtension.setSession(TEST_KEYSPACE_SESSION);

        TEST_KEYSPACE_SESSION.execute("INSERT INTO one_key (user_id, number) VALUES (?, 1);", userId);
        TEST_KEYSPACE_SESSION.execute("INSERT INTO two_keys (user_id, account_id, number) VALUES (?, ?, 1);", userId, accountId2);

        deleteUserDataExtension.setExcludedTables("one_key");

        Assertions.assertDoesNotThrow(() -> deleteUserDataExtension.beforeTestExecution(null));

        TEST_KEYSPACE_SESSION.execute("DELETE FROM two_keys WHERE user_id = ?;", userId);

        Assertions.assertDoesNotThrow(() -> deleteUserDataExtension.afterTestExecution(null));
    }

    @Test
    void shouldAssertWhenMissingDataBeforeDeletion() {
        deleteUserDataExtension.setUserId(userId);
        deleteUserDataExtension.setSession(TEST_KEYSPACE_SESSION);

        Assertions.assertThrows(
                AssertionError.class,
                () -> deleteUserDataExtension.beforeTestExecution(null),
                "Unsatisfied precondition: expecting some data for userId " + userId + " in table(s): [one_key, two_keys]"
        );
    }

    @Test
    void shouldAssertWhenDataNotDeleted() {
        deleteUserDataExtension.setUserId(userId);
        deleteUserDataExtension.setSession(TEST_KEYSPACE_SESSION);

        TEST_KEYSPACE_SESSION.execute("INSERT INTO one_key (user_id, number) VALUES (?, 1);", userId);
        TEST_KEYSPACE_SESSION.execute("INSERT INTO two_keys (user_id, account_id, number) VALUES (?, ?, 1);", userId, accountId1);

        Assertions.assertDoesNotThrow(() -> deleteUserDataExtension.beforeTestExecution(null));

        Assertions.assertThrows(
                AssertionError.class,
                () -> deleteUserDataExtension.afterTestExecution(null),
                "Expecting no data for userId " + userId + " in table(s) [one_key, two_keys]"
        );
    }
}
