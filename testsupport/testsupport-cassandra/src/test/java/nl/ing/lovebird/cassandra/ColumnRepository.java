package nl.ing.lovebird.cassandra;

import com.datastax.driver.core.Session;
import org.springframework.stereotype.Repository;

@Repository
public class ColumnRepository extends CassandraRepository<Column> {

    public ColumnRepository(final Session session) {
        super(session, Column.class, "system_schema");
    }
}
