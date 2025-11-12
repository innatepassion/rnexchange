package com.rnexchange.domain;

import static com.rnexchange.domain.ExecutionTestSamples.*;
import static com.rnexchange.domain.OrderTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ExecutionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Execution.class);
        Execution execution1 = getExecutionSample1();
        Execution execution2 = new Execution();
        assertThat(execution1).isNotEqualTo(execution2);

        execution2.setId(execution1.getId());
        assertThat(execution1).isEqualTo(execution2);

        execution2 = getExecutionSample2();
        assertThat(execution1).isNotEqualTo(execution2);
    }

    @Test
    void orderTest() {
        Execution execution = getExecutionRandomSampleGenerator();
        Order orderBack = getOrderRandomSampleGenerator();

        execution.setOrder(orderBack);
        assertThat(execution.getOrder()).isEqualTo(orderBack);

        execution.order(null);
        assertThat(execution.getOrder()).isNull();
    }
}
