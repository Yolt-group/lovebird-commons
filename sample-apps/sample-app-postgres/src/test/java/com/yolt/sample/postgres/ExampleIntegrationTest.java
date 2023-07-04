package com.yolt.sample.postgres;

import nl.ing.lovebird.postgres.test.EnableExternalPostgresTestDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@EnableExternalPostgresTestDatabase
class ExampleIntegrationTest {

    final UUID userId = UUID.randomUUID();

    @Autowired
    TeaService service;

    @Test
    void thereIsNoTeaForNewUser() {
        List<TeaEntity> allTeaForUser = service.findAll(userId);
        assertThat(allTeaForUser).isEmpty();
    }

    @Test
    void weCanAddEarlGreyForUser() {
        service.addEarlGrey(userId);
        List<TeaEntity> allTeaForUser = service.findAll(userId);
        assertThat(allTeaForUser)
                .isNotEmpty()
                .allMatch(tea -> tea.getUserId().equals(userId))
                .allMatch(tea -> tea.getTeaId().equals(TeaService.earlGreyId));
    }

}
