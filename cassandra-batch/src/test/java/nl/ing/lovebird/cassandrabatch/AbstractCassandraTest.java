package nl.ing.lovebird.cassandrabatch;

import com.datastax.driver.core.Session;
import nl.ing.lovebird.cassandra.test.TestCassandraSession;
import nl.ing.lovebird.cassandrabatch.pager.Transaction;
import nl.ing.lovebird.cassandrabatch.pager.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

public abstract class AbstractCassandraTest {

    protected static final Session SESSION = TestCassandraSession.provide(Transaction.KEYSPACE, Paths.get("src/test/resources/schema.cql"));

    protected TransactionRepository transactionRepository;

    @BeforeEach
    public void setUp() throws IOException {
        transactionRepository = new TransactionRepository(SESSION);
    }

    @AfterEach
    void tearDown() {
        transactionRepository.truncate();
    }

    protected Transaction insertTransaction(final int number) {
        return insertTransaction(new UUID(0, 0), number);
    }

    protected Transaction insertTransaction(final UUID userId, final int number) {
        final Transaction transaction = Transaction.builder()
                .userId(userId)
                .transactionId(new UUID(0, number))
                .name("transaction_" + number)
                .build();
        transactionRepository.save(transaction);
        return transaction;
    }
}
