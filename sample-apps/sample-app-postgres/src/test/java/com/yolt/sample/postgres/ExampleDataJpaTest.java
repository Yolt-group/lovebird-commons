package com.yolt.sample.postgres;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ExampleDataJpaTest {

    final UUID userId = UUID.randomUUID();
    final UUID teaId = UUID.randomUUID();

    @Autowired
    TeaRepository repository;

    @Test
    void weCanAddEarlGreyForUser() {
        TeaEntity teaEntity = TeaEntity.builder()
                .teaId(teaId)
                .userId(userId)
                .amount(BigDecimal.ONE)
                .build();

        repository.save(teaEntity);

        List<TeaEntity> allTeaForUser = repository.findAllByUserId(userId);
        assertThat(allTeaForUser)
                .isNotEmpty()
                .allMatch(tea -> tea.getUserId().equals(userId))
                .allMatch(tea -> tea.getTeaId().equals(teaId));
    }

}
