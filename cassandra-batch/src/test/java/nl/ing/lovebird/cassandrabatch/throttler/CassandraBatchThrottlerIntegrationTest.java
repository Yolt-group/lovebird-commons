package nl.ing.lovebird.cassandrabatch.throttler;

import com.datastax.driver.core.PagingState;
import nl.ing.lovebird.cassandrabatch.AbstractCassandraTest;
import nl.ing.lovebird.cassandrabatch.pager.EntitiesPage;
import nl.ing.lovebird.cassandrabatch.pager.SelectAllEntityPager;
import nl.ing.lovebird.cassandrabatch.pager.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CassandraBatchThrottlerIntegrationTest extends AbstractCassandraTest {

    private final List<Transaction> allTransactions = new ArrayList<>();

    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();

        for (int i = 0; i < 50; i++) {
            allTransactions.add(insertTransaction(i));
        }
    }

    @Test
    void startBatch_Below1TpsNotAllowed() {
        assertThrows(IllegalArgumentException.class, () -> new CassandraBatchThrottler(0));
    }

    @Test
    void startBatch_MaximumSpeed() {
        CassandraBatchThrottler cassandraBatchThrottler =
                new CassandraBatchThrottler(Integer.MAX_VALUE);

        long startTime = System.currentTimeMillis();

        List<Transaction> fetchedTransactions = new ArrayList<>();
        cassandraBatchThrottler.startBatch((pagingState, pageSize) ->
                getThrottledTaskResult(fetchedTransactions, pagingState, pageSize));

        long runtime = System.currentTimeMillis() - startTime;
        int fiveSeconds = 5000;
        assertThat(runtime).isLessThan(fiveSeconds);
        assertThat(fetchedTransactions).containsAll(allTransactions);
        assertThat(fetchedTransactions.size()).isEqualTo(allTransactions.size());
    }

    @Test
    void startBatch_ExceptionsAreHandled() {
        CassandraBatchThrottler cassandraBatchThrottler =
                new CassandraBatchThrottler(Integer.MAX_VALUE);

        assertDoesNotThrow(() -> cassandraBatchThrottler.startBatch((pagingState, pageSize) ->
        {
            //Should be catched by the throttler, not rethrown
            throw new RuntimeException();
        }));
    }

    @Test
    void startBatch_5Tps() {
        CassandraBatchThrottler cassandraBatchThrottler =
                new CassandraBatchThrottler(5);

        long startTime = System.currentTimeMillis();

        List<Transaction> fetchedTransactions = new ArrayList<>();
        cassandraBatchThrottler.startBatch((pagingState, pageSize) ->
                getThrottledTaskResult(fetchedTransactions, pagingState, pageSize));

        long runtime = System.currentTimeMillis() - startTime;
        int fiveSeconds = 5000;
        assertThat(runtime).isGreaterThan(fiveSeconds);
        assertThat(fetchedTransactions).containsAll(allTransactions);
        assertThat(fetchedTransactions.size()).isEqualTo(allTransactions.size());
    }

    private ThrottledTaskResult getThrottledTaskResult(final List<Transaction> fetchedTransactions, final PagingState pagingState, final int pageSize) {
        SelectAllEntityPager<Transaction> selectAllEntityPager = new SelectAllEntityPager<>(SESSION, transactionRepository.getMapper(), pageSize);

        EntitiesPage<Transaction> page = selectAllEntityPager.getNextPage(Transaction.TABLE_NAME, pagingState);

        fetchedTransactions.addAll(page.getEntities());

        return new ThrottledTaskResult(page.getPagingState(), page.getEntities().size(), 0);
    }
}
