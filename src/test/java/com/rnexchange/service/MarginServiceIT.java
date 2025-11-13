package com.rnexchange.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.enumeration.OrderSide;
import com.rnexchange.domain.enumeration.OrderType;
import com.rnexchange.service.dto.MarginAssessment;
import com.rnexchange.service.dto.TraderOrderRequest;
import com.rnexchange.service.seed.BaselineSeedService;
import com.rnexchange.service.seed.dto.BaselineSeedRequest;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class MarginServiceIT extends com.rnexchange.service.seed.AbstractBaselineSeedIT {

    @Autowired
    private BaselineSeedService baselineSeedService;

    @Autowired
    private MarginService marginService;

    @BeforeEach
    void setUp() {
        BaselineSeedRequest request = BaselineSeedRequest.builder().force(true).invocationId(UUID.randomUUID()).build();
        baselineSeedService.runBaselineSeedBlocking(request);
    }

    @Test
    void evaluateMarginForSeededCashInstrument() {
        TraderOrderRequest request = TraderOrderRequest.builder()
            .traderLogin("trader-one")
            .instrumentSymbol("RELIANCE")
            .side(OrderSide.BUY)
            .type(OrderType.MARKET)
            .tif(com.rnexchange.domain.enumeration.Tif.DAY)
            .quantity(new BigDecimal("10"))
            .price(new BigDecimal("2200.00"))
            .build();

        MarginAssessment assessment = marginService.evaluateMargin(request);

        assertThat(assessment.sufficient()).isTrue();
        assertThat(assessment.initialRequirement()).isEqualByComparingTo("4400.00");
        assertThat(assessment.maintenanceRequirement()).isEqualByComparingTo("3300.00");
        assertThat(assessment.availableBalance()).isEqualByComparingTo("1000000.00");
        assertThat(assessment.remainingBalance()).isEqualByComparingTo("995600.00");
    }

    @Test
    void throwsWhenMarginInsufficient() {
        TraderOrderRequest request = TraderOrderRequest.builder()
            .traderLogin("trader-one")
            .instrumentSymbol("RELIANCE")
            .side(OrderSide.BUY)
            .type(OrderType.MARKET)
            .tif(com.rnexchange.domain.enumeration.Tif.DAY)
            .quantity(new BigDecimal("2000"))
            .price(new BigDecimal("5000.00"))
            .build();

        assertThatThrownBy(() -> marginService.evaluateMargin(request))
            .isInstanceOf(InsufficientMarginException.class)
            .hasMessageContaining("Insufficient margin")
            .satisfies(ex -> {
                if (ex instanceof InsufficientMarginException marginException) {
                    MarginAssessment assessment = marginException.getAssessment();
                    assertThat(assessment.sufficient()).isFalse();
                    assertThat(assessment.initialRequirement()).isGreaterThan(assessment.availableBalance());
                }
            });
    }
}
