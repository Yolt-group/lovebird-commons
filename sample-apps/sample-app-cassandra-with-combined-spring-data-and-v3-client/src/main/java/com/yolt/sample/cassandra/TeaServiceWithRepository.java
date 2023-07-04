package com.yolt.sample.cassandra;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeaServiceWithRepository {

    public static final UUID earlGreyId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final TeaRepository repository;

    public void addEarlGrey(UUID userId) {
        TeaEntitySpringData tea = TeaEntitySpringData.builder()
                .teaId(earlGreyId)
                .userId(userId)
                .amount(BigDecimal.ONE)
                .build();
        repository.save(tea);
    }

    public List<TeaEntitySpringData> findAll(UUID userId) {
        return repository.findAllByUserId(userId);
    }

}