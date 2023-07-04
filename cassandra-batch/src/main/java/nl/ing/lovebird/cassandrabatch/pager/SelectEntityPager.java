package nl.ing.lovebird.cassandrabatch.pager;

import com.datastax.driver.core.PagingState;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * Pager for adding pagination to queries.
 * <p/>
 *
 * @param <T> type of the entity you wish to paginate over.
 */
public class SelectEntityPager<T> {

    private final Session session;
    private final Mapper<T> mapper;
    private final int pageSize;

    private final Statement select;

    private SelectEntityPager(Session session, Mapper<T> mapper, int pageSize, Statement select) {
        this.session = session;
        this.mapper = mapper;
        this.pageSize = pageSize;
        this.select = select;
    }

    public static <T> SelectEntityPager<T> from(Session session, Mapper<T> mapper, int pageSize, Statement select) {
        return new SelectEntityPager<>(session, mapper, pageSize, select);
    }

    public static <T> SelectEntityPager<T> selectAll(
            Session session,
            Mapper<T> mapper,
            int pageSize,
            String tableName
    ) {
        Select select = QueryBuilder
                .select().all()
                .from(tableName);
        return new SelectEntityPager<>(session, mapper, pageSize, select);
    }

    public EntitiesPage<T> getNextPage(PagingState pagingState) {
        select.setFetchSize(pageSize);
        select.setPagingState(pagingState);

        ResultSet resultSet = session.execute(select);

        PagingState newPagingState = resultSet.getExecutionInfo().getPagingState();
        int remaining = resultSet.getAvailableWithoutFetching();

        List<T> entities = new ArrayList<>();
        Result<T> result = mapper.map(resultSet);
        for (T trx : result) {
            entities.add(trx);

            // Prevent the driver from retrieving more data (which would happen if we keep calling converter.read())
            if (--remaining == 0) {
                break;
            }
        }

        return new EntitiesPage<>(entities, newPagingState);
    }

}
