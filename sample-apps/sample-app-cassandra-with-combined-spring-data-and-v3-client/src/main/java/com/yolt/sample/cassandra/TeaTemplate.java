package com.yolt.sample.cassandra;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

@Repository
public class TeaTemplate {

    private final CassandraTemplate cassandraTemplate;

    @Autowired
    public TeaTemplate(final CassandraTemplate cassandraTemplate) {
        this.cassandraTemplate = cassandraTemplate;
    }

    public void save(TeaEntitySpringData entity) {
        cassandraTemplate.insert(entity);
    }

    public void delete(TeaEntitySpringData entity) {
        cassandraTemplate.delete(entity);
    }

    public List<TeaEntitySpringData> findAllByUserId(UUID userId) {
        return cassandraTemplate.query(TeaEntitySpringData.class).matching(
                query(
                        where(TeaEntitySpringData.USER_ID_COLUMN).is(userId)
                )
        ).all();
    }
}