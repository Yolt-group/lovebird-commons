package nl.ing.lovebird.cassandrabatch.pager;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import nl.ing.lovebird.cassandra.CassandraRepository;

public class TransactionRepository extends CassandraRepository<Transaction> {
    public TransactionRepository(Session session) {
        super(session, Transaction.class);
    }

    public void save(Transaction transaction) {
        super.save(transaction);
    }

    public Mapper<Transaction> getMapper() {
        return this.mapper;
    }

    public void truncate() {
        select(createSelect().allowFiltering()).forEach(this::delete);
    }
}
