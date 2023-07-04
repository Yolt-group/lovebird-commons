package com.yolt.sample.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TeaRepository extends CassandraRepository<TeaEntitySpringData, UUID> {

    List<TeaEntitySpringData> findAllByUserId(UUID userId);
}