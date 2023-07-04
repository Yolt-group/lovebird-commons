package nl.ing.lovebird.cassandrabatch.pager;

import nl.ing.lovebird.cassandrabatch.AbstractCassandraTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SuppressWarnings("deprecation")
class SelectAllEntityPagerIntegrationTest extends AbstractCassandraTest {

    final List<Transaction> transactions = new ArrayList<>();

    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();

        for (int i = 0; i < 7; i++) {
            transactions.add(insertTransaction(i));
        }
    }

    @Test
    void testGetPage() {
        SelectAllEntityPager<Transaction> selectAllEntityPager =
                new SelectAllEntityPager<>(SESSION, transactionRepository.getMapper(), 3);

        EntitiesPage<Transaction> page = selectAllEntityPager.getNextPage(Transaction.TABLE_NAME, null);
        assertNotNull(page.getPagingState());
        assertThat(page.getEntities()).containsExactly(transactions.get(0), transactions.get(1), transactions.get(2));

        page = selectAllEntityPager.getNextPage(Transaction.TABLE_NAME, page.getPagingState());
        assertNotNull(page.getPagingState());
        assertThat(page.getEntities()).containsExactly(transactions.get(3), transactions.get(4), transactions.get(5));

        page = selectAllEntityPager.getNextPage(Transaction.TABLE_NAME, page.getPagingState());
        assertNull(page.getPagingState());
        assertThat(page.getEntities()).containsExactly(transactions.get(6));
    }
}
