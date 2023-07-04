package nl.ing.lovebird.cassandrabatch.pager;

import com.datastax.driver.core.PagingState;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import nl.ing.lovebird.cassandrabatch.AbstractCassandraTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static nl.ing.lovebird.cassandrabatch.pager.SelectEntityPager.from;
import static nl.ing.lovebird.cassandrabatch.pager.SelectEntityPager.selectAll;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SelectEntityPagerIntegrationTest extends AbstractCassandraTest {
    static final int PAGE_SIZE = 3;

    final List<Transaction> transactions = new ArrayList<>();

    final UUID myUserid = UUID.randomUUID();
    final UUID otherUserId = UUID.randomUUID();

    @BeforeEach
    public void setUp() throws IOException {
        super.setUp(); // Repository initialization happens here.

        for (int i = 0; i < 3; i++) {
            transactions.add(insertTransaction(otherUserId, i));
        }
        for (int i = 0; i < 7; i++) {
            transactions.add(insertTransaction(myUserid, i));
        }
    }

    @Test
    void testGetNextPage() {
        Statement select = QueryBuilder.select()
                .from(Transaction.TABLE_NAME).where(eq(Transaction.USER_ID_COLUMN, myUserid));

        SelectEntityPager<Transaction> selectEntityPager = from(SESSION, transactionRepository.getMapper(), PAGE_SIZE, select);

        EntitiesPage<Transaction> page = selectEntityPager.getNextPage(null);
        assertNotNull(page.getPagingState());
        assertThat(page.getEntities()).containsExactly(transactions.get(3), transactions.get(4), transactions.get(5));

        page = selectEntityPager.getNextPage(page.getPagingState());
        assertNotNull(page.getPagingState());
        assertThat(page.getEntities()).containsExactly(transactions.get(6), transactions.get(7), transactions.get(8));

        page = selectEntityPager.getNextPage(page.getPagingState());
        assertNull(page.getPagingState());
        assertThat(page.getEntities()).containsExactly(transactions.get(9));
    }

    @Test
    void testGetNextPageWithSelectAll() {
        SelectEntityPager<Transaction> selectEntityPager = selectAll(SESSION, transactionRepository.getMapper(), PAGE_SIZE, Transaction.TABLE_NAME);

        EntitiesPage<Transaction> page = selectEntityPager.getNextPage(null);
        Collection<Transaction> selectedTransactions = new ArrayList<>(page.getEntities());
        while (page.getPagingState() != null) {
            PagingState pagingState = page.getPagingState();
            page = selectEntityPager.getNextPage(pagingState);
            selectedTransactions.addAll(page.getEntities());
        }
        assertThat(selectedTransactions).containsAll(transactions);
    }
}
