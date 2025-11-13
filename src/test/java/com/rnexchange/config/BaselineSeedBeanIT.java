package com.rnexchange.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.RnexchangeApp;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = RnexchangeApp.class, properties = "spring.liquibase.enabled=false")
@ActiveProfiles("dev")
class BaselineSeedBeanIT {

    @Autowired
    private SpringLiquibase liquibase;

    @Test
    void fakerContextRemainsDisabledInDevProfile() {
        String contexts = liquibase.getContexts();
        assertThat(contexts).as("Liquibase contexts should include baseline").contains("baseline");
        assertThat(contexts).as("Liquibase contexts should not include faker").doesNotContain("faker");
    }
}
