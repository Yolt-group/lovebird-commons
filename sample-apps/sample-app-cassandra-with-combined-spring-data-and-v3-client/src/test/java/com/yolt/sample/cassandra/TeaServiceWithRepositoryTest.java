package com.yolt.sample.cassandra;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class TeaServiceWithRepositoryTest {

    final UUID userId = UUID.randomUUID();

    @Autowired
    TeaServiceWithRepository service;

    @Test
    void thereIsNoTeaForNewUser() {
        List<TeaEntitySpringData> allTeaForUser = service.findAll(userId);
        assertThat(allTeaForUser).isEmpty();
    }

    @Test
    void weCanAddEarlGreyForUser() {
        service.addEarlGrey(userId);
        List<TeaEntitySpringData> allTeaForUser = service.findAll(userId);
        assertThat(allTeaForUser)
                .isNotEmpty()
                .allMatch(tea -> tea.getUserId().equals(userId))
                .allMatch(tea -> tea.getTeaId().equals(TeaServiceWithRepository.earlGreyId));
    }

}
