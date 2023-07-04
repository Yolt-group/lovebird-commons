package com.yolt.sample.cassandra;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeaServiceWithV3Client {

    static final UUID earlGreyId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final TeaRepositoryWithV3Client repository;

    public void addEarlGrey(UUID userId) {
        TeaEntityCassandraV3 tea = TeaEntityCassandraV3.builder()
                .teaId(earlGreyId)
                .userId(userId)
                .amount(BigDecimal.ONE)
                .build();
        repository.save(tea);
    }

    public List<TeaEntityCassandraV3> findAll(UUID userId) {
        return repository.findAllByUserId(userId);
    }

}