package com.yolt.sample.cassandra;

import com.datastax.driver.core.Session;
import nl.ing.lovebird.cassandra.CassandraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

@Repository
public class TeaRepositoryWithV3Client extends CassandraRepository<TeaEntityCassandraV3> {

    @Autowired
    public TeaRepositoryWithV3Client(Session session) {
        super(session, TeaEntityCassandraV3.class);
    }

    @Override
    public void save(TeaEntityCassandraV3 entity) {
        super.save(entity);
    }

    @Override
    public void delete(TeaEntityCassandraV3 entity) {
        super.delete(entity);
    }

    public List<TeaEntityCassandraV3> findAllByUserId(UUID userId) {
        return super.select(eq(TeaEntityCassandraV3.USER_ID_COLUMN, userId));
    }
}