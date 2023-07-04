package nl.ing.lovebird.cassandrabatch.pager;

import com.datastax.driver.core.PagingState;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated Use {@link SelectEntityPager} instead.
 */
@Deprecated
public class SelectAllEntityPager<T> {

    private final Session session;
    private final Mapper<T> mapper;
    private final int pageSize;

    public SelectAllEntityPager(final Session session, final Mapper<T> mapper, final int pageSize) {
        this.session = session;
        this.mapper = mapper;
        this.pageSize = pageSize;
    }

    public EntitiesPage<T> getNextPage(final String tableName, final PagingState pagingState) {
        Select select = QueryBuilder
                .select().all()
                .from(tableName);

        select.setFetchSize(pageSize);
        select.setPagingState(pagingState);

        final ResultSet resultSet = session.execute(select);

        final PagingState newPagingState = resultSet.getExecutionInfo().getPagingState();
        int remaining = resultSet.getAvailableWithoutFetching();

        final List<T> entities = new ArrayList<>();
        final Result<T> result = mapper.map(resultSet);
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
