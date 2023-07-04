package nl.ing.lovebird.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

public class UserRepository extends CassandraRepository<User> {
    public UserRepository(Session session) {
        super(session, User.class);
    }

    public UserRepository(Session session, String overrideKeyspace) {
        super(session, User.class, overrideKeyspace);
    }

    public UserRepository(Session session, ConsistencyLevel readCl, ConsistencyLevel writeCl) {
        super(session, User.class, readCl, writeCl, null);
    }

    public long count() {
        return super.count();
    }

    public void save(User user) {
        super.save(user);
    }

    public void save(List<User> users) {
        super.save(users);
    }

    public void delete(User user) {
        super.delete(user);
    }

    public void delete(List<User> users) {
        super.delete(users);
    }

    public Optional<User> selectOne(UUID userId) {
        Select select = QueryBuilder.select().from(keyspace,"users");

        select.where(eq("user_id", userId));

        return selectOne(select);
    }
}
