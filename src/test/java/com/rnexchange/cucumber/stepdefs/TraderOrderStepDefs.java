package com.rnexchange.cucumber.stepdefs;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.domain.enumeration.OrderSide;
import com.rnexchange.domain.enumeration.OrderStatus;
import com.rnexchange.domain.enumeration.OrderType;
import com.rnexchange.domain.enumeration.Tif;
import com.rnexchange.service.OrderService;
import com.rnexchange.service.dto.TraderOrderRequest;
import com.rnexchange.service.dto.TraderOrderResult;
import com.rnexchange.service.seed.BaselineSeedService;
import com.rnexchange.service.seed.dto.BaselineSeedRequest;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;

public class TraderOrderStepDefs extends StepDefs {

    @Autowired
    private BaselineSeedService baselineSeedService;

    @Autowired
    private OrderService orderService;

    private TraderOrderResult lastResult;
    private RuntimeException lastFailure;

    @Before
    public void resetState() {
        lastResult = null;
        lastFailure = null;
    }

    @Given("the baseline seed job has completed successfully")
    public void theBaselineSeedJobHasCompletedSuccessfully() {
        BaselineSeedRequest request = BaselineSeedRequest.builder().force(true).invocationId(UUID.randomUUID()).build();
        baselineSeedService.runBaselineSeedBlocking(request);
    }

    @When("trader {string} submits a {string} market order for instrument {string} with quantity {long} at price {double}")
    public void traderSubmitsOrder(String traderLogin, String side, String instrument, long quantity, double price) {
        try {
            TraderOrderRequest request = TraderOrderRequest.builder()
                .traderLogin(traderLogin)
                .instrumentSymbol(instrument)
                .side(OrderSide.valueOf(side))
                .type(OrderType.MARKET)
                .tif(Tif.DAY)
                .quantity(BigDecimal.valueOf(quantity))
                .price(BigDecimal.valueOf(price))
                .build();
            lastResult = orderService.submitTraderOrder(request);
        } catch (RuntimeException ex) {
            lastFailure = ex;
        }
    }

    @Then("the submitted order status is {string}")
    public void theSubmittedOrderStatusIs(String expectedStatus) {
        assertThat(lastResult).as("Expected order submission to succeed").isNotNull();
        assertThat(lastResult.order().getStatus()).isEqualTo(OrderStatus.valueOf(expectedStatus));
    }

    @And("the margin check recorded initial requirement {string} and remaining balance {string}")
    public void theMarginCheckRecordedValues(String expectedInitial, String expectedRemaining) {
        assertThat(lastResult).isNotNull();
        assertThat(lastResult.marginAssessment()).isNotNull();
        assertThat(lastResult.marginAssessment().initialRequirement()).isEqualByComparingTo(expectedInitial);
        assertThat(lastResult.marginAssessment().remainingBalance()).isEqualByComparingTo(expectedRemaining);
    }

    @Then("the order submission is rejected with reason containing {string}")
    public void theOrderSubmissionIsRejectedWithReasonContaining(String reason) {
        assertThat(lastFailure).as("Expected order submission to fail").isNotNull();
        assertThat(lastFailure.getMessage()).contains(reason);
    }
}
