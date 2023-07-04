package com.yolt.sample.postgres;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeaService {

    static final UUID earlGreyId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final TeaRepository repository;

    public void addEarlGrey(UUID userId) {
        TeaEntity tea = TeaEntity.builder()
                .teaId(earlGreyId)
                .userId(userId)
                .amount(BigDecimal.ONE)
                .build();
        repository.save(tea);
    }

    public List<TeaEntity> findAll(UUID userId) {
        return repository.findAllByUserId(userId);
    }

}