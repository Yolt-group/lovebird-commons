package nl.ing.lovebird.testsupport.cassandra.listeners;

import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import java.util.Collections;
import java.util.List;

/**
 * A {@see org.springframework.test.context.TestExecutionListener} that truncates ALL the C* tables in the keyspace bound
 * to the current @{see {@link Session} before and after every test.
 * <p/>
 * Usage:
 * <pre>
 *  \@RunWith(SpringRunner.class)
 *  \@TestExecutionListeners(
 *      listeners = {KeyspaceTruncatingTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
 *  )
 *  public abstract class BaseIntegrationTest { ...}
 * </pre>
 *
 * @deprecated We run all all model mutation .cql files at application startup. Truncating tables will
 * remove this data. This means that one test will have the data, the next won't. This results in flakey tests.
 * <p>
 * Prefer creating users/accounts/transactions/ect with random UUIDs instead.
 */
@Deprecated
public class KeyspaceTruncatingTestExecutionListener extends AbstractTestExecutionListener {

    private static final List<String> EXCLUDED_TABLES = Collections.singletonList("modelmutation");

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Session session;

    @Override
    public void prepareTestInstance(TestContext testContext) {
        session = testContext.getApplicationContext().getBean(Session.class);
    }

    @Override
    public void beforeTestMethod(TestContext testContext) {
        truncateTables();
    }

    @Override
    public void afterTestMethod(TestContext testContext) {
        truncateTables();
    }

    private void truncateTables() {
        if (session.getLoggedKeyspace() == null)
            return;

        final KeyspaceMetadata keyspace = session.getCluster().getMetadata().getKeyspace(session.getLoggedKeyspace());
        logger.info("Truncating tables in keyspace {}", keyspace.getName()); //NOSHERIFF runs only in test context
        keyspace.getTables()
                .stream()
                .filter(tableMetadata -> !EXCLUDED_TABLES.contains(tableMetadata.getName()))
                .forEach(tableMetadata -> {
                            logger.debug("Truncating {}", tableMetadata.getName());
                            session.execute("TRUNCATE TABLE " + tableMetadata.getName());
                        }
                );
    }
}
