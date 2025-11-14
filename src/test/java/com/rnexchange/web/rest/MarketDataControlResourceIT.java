package com.rnexchange.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.enumeration.AssetClass;
import com.rnexchange.domain.enumeration.Currency;
import com.rnexchange.domain.enumeration.ExchangeStatus;
import com.rnexchange.repository.ExchangeRepository;
import com.rnexchange.repository.InstrumentRepository;
import com.rnexchange.service.dto.FeedState;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
@jakarta.transaction.Transactional
class MarketDataControlResourceIT {

    private static final String START_URL = "/api/marketdata/mock/start";
    private static final String STOP_URL = "/api/marketdata/mock/stop";
    private static final String STATUS_URL = "/api/marketdata/mock/status";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private ExchangeRepository exchangeRepository;

    @BeforeEach
    void setUp() {
        String exchangeCode = "TESTX";
        com.rnexchange.domain.Exchange exchange = exchangeRepository
            .findOneByCode(exchangeCode)
            .orElseGet(() ->
                exchangeRepository.save(
                    new com.rnexchange.domain.Exchange()
                        .code(exchangeCode)
                        .name("Test Exchange")
                        .timezone("Asia/Kolkata")
                        .status(ExchangeStatus.ACTIVE)
                )
            );

        com.rnexchange.domain.Instrument instrument = new com.rnexchange.domain.Instrument()
            .symbol("TEST_SYMBOL")
            .name("Test Instrument")
            .assetClass(AssetClass.EQUITY)
            .exchangeCode(exchangeCode)
            .tickSize(new BigDecimal("0.05"))
            .lotSize(1L)
            .currency(Currency.INR)
            .status("ACTIVE")
            .exchange(exchange);
        instrumentRepository.save(instrument);
    }

    @Test
    @WithMockUser(username = "exchange-operator", authorities = "EXCHANGE_OPERATOR")
    void startFeedReturnsRunningStatus() throws Exception {
        mockMvc
            .perform(post(START_URL).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.globalState").value(FeedState.RUNNING.name()));

        mockMvc.perform(get(STATUS_URL).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "exchange-operator", authorities = "EXCHANGE_OPERATOR")
    void stopFeedReturnsStoppedStatus() throws Exception {
        mockMvc.perform(post(START_URL).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        mockMvc
            .perform(post(STOP_URL).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.globalState").value(FeedState.STOPPED.name()));
    }

    @Test
    @WithMockUser(username = "trader-one", authorities = "TRADER")
    void traderCannotStartFeed() throws Exception {
        mockMvc.perform(post(START_URL).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }
}
