package com.yolt.sample.postgres;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TeaRepository extends CrudRepository<TeaEntity, UUID> {

    List<TeaEntity> findAllByUserId(final UUID userId);
}