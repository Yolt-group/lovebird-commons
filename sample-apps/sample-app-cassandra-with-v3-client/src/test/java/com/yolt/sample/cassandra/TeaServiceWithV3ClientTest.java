package com.yolt.sample.cassandra;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class TeaServiceWithV3ClientTest {

    final UUID userId = UUID.randomUUID();

    @Autowired
    TeaServiceWithV3Client service;

    @Test
    void thereIsNoTeaForNewUser() {
        List<TeaEntityCassandraV3> allTeaForUser = service.findAll(userId);
        assertThat(allTeaForUser).isEmpty();
    }

    @Test
    void weCanAddEarlGreyForUser() {
        service.addEarlGrey(userId);
        List<TeaEntityCassandraV3> allTeaForUser = service.findAll(userId);
        assertThat(allTeaForUser)
                .isNotEmpty()
                .allMatch(tea -> tea.getUserId().equals(userId))
                .allMatch(tea -> tea.getTeaId().equals(TeaServiceWithV3Client.earlGreyId));
    }

}
