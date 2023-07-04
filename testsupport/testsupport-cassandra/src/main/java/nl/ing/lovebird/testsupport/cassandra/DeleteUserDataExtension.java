package nl.ing.lovebird.testsupport.cassandra;

import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

public class DeleteUserDataExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
    @Setter
    private UUID userId;
    @Setter
    private Session session;

    /**
     * Enables to specify the list of tables, which should be excluded from the verification.
     * This might be useful if some tables with user data are not cleared immediately after user deletion,
     * e.g. usually the deletion of rows from is table is delayed for some reason.
     */
    private Set<String> excludedTables = new HashSet<>();

    public void setExcludedTables(String... tablesToExclude) {
        excludedTables = new HashSet<>(Arrays.asList(tablesToExclude));
    }

    /**
     * Iterates all tables in the keyspace where {@code user_id} is the first component of the primary key.
     * It throws an exception when some of those tables do not contain rows for given {@code userId}.
     */
    @Override
    public void beforeTestExecution(ExtensionContext context) {
        Assertions.assertNotNull(userId);
        Assertions.assertNotNull(session);

        final KeyspaceMetadata keyspace = session.getCluster().getMetadata().getKeyspace(session.getLoggedKeyspace());
        final Set<String> unpopulatedTables = new HashSet<>();
        keyspace.getTables().forEach(tableMetadata -> {
            if (shouldCheckTable(tableMetadata) && getRowCount(tableMetadata.getName()) == 0) {
                unpopulatedTables.add(tableMetadata.getName());
            }
        });

        Assertions.assertTrue(
                unpopulatedTables.isEmpty(),
                String.format("Unsatisfied precondition: expecting some data for userId %s in table(s): %s", userId, unpopulatedTables));
    }

    /**
     * Iterates all tables in the keyspace where {@code user_id} is the first component of the primary key.
     * It throws an exception when some of those tables contain rows for given {@code userId}.
     */
    @Override
    public void afterTestExecution(ExtensionContext context) {
        Assertions.assertNotNull(userId);
        Assertions.assertNotNull(session);

        final KeyspaceMetadata keyspace = session.getCluster().getMetadata().getKeyspace(session.getLoggedKeyspace());
        final Set<String> stillPopulatedTables = new HashSet<>();
        keyspace.getTables().forEach(tableMetadata -> {
            if (shouldCheckTable(tableMetadata) && getRowCount(tableMetadata.getName()) > 0) {
                stillPopulatedTables.add(tableMetadata.getName());
            }
        });

        Assertions.assertTrue(
                stillPopulatedTables.isEmpty(),
                String.format("Expecting no data for userId %s in table(s) %s", userId, stillPopulatedTables));
    }

    /**
     * Returns {@code true} when first component of the primary key is field with name {@code user_id}
     * AND
     * the table is not excluded from the verification
     */
    private boolean shouldCheckTable(final TableMetadata tableMetadata) {
        if (!tableMetadata.getPrimaryKey().get(0).getName().equals("user_id")) {
            return false;
        }
        return !excludedTables.contains(tableMetadata.getName());
    }

    private long getRowCount(final String tableName) {
        final ResultSet resultSet = session.execute(
                QueryBuilder.select()
                        .countAll()
                        .from(tableName)
                        .where(eq("user_id", userId))
                        .allowFiltering());
        return resultSet.one().getLong(0);
    }
}
