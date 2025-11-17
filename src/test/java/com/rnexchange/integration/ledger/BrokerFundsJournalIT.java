package com.rnexchange.integration.ledger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.Broker;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.domain.enumeration.AccountStatus;
import com.rnexchange.domain.enumeration.AccountType;
import com.rnexchange.domain.enumeration.Currency;
import com.rnexchange.repository.BrokerRepository;
import com.rnexchange.repository.LedgerEntryRepository;
import com.rnexchange.repository.TradingAccountRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test for broker-created journal entries adjusting balances, including negative balances.
 * Per M6 User Story 2 (T023).
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(username = "broker_demo", roles = { "BROKER_ADMIN" })
class BrokerFundsJournalIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TradingAccountRepository tradingAccountRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private BrokerRepository brokerRepository;

    private TradingAccount testAccount;
    private BigDecimal initialBalance;

    @BeforeEach
    void setup() {
        testAccount = tradingAccountRepository
            .findAll()
            .stream()
            .findFirst()
            .orElseGet(() -> {
                Broker broker = brokerRepository.findAll().stream().findFirst().orElseThrow();
                TradingAccount ta = new TradingAccount();
                ta.setBroker(broker);
                ta.setType(AccountType.CASH);
                ta.setBaseCcy(Currency.USD);
                ta.setBalance(new BigDecimal("100.00"));
                ta.setStatus(AccountStatus.ACTIVE);
                return tradingAccountRepository.saveAndFlush(ta);
            });
        initialBalance = testAccount.getBalance();
    }

    @Test
    @Transactional
    void journalCreditIncreasesBalance() throws Exception {
        long beforeCount = ledgerEntryRepository.count();
        BigDecimal creditAmount = new BigDecimal("50.00");

        String body = String.format(
            """
            {
              "createdAt": "2024-01-15T10:00:00Z",
              "type": "CREDIT",
              "amount": %s,
              "ccy": "USD",
              "description": "Test credit",
              "tradingAccount": {
                "id": %d
              }
            }
            """,
            creditAmount,
            testAccount.getId()
        );

        mockMvc.perform(post("/api/ledger-entries").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());

        TradingAccount updated = tradingAccountRepository.findById(testAccount.getId()).orElseThrow();
        assertThat(updated.getBalance()).isEqualByComparingTo(initialBalance.add(creditAmount));
        assertThat(ledgerEntryRepository.count()).isEqualTo(beforeCount + 1);
    }

    @Test
    @Transactional
    void journalDebitDecreasesBalance() throws Exception {
        long beforeCount = ledgerEntryRepository.count();
        BigDecimal debitAmount = new BigDecimal("30.00");

        String body = String.format(
            """
            {
              "createdAt": "2024-01-15T10:00:00Z",
              "type": "DEBIT",
              "amount": %s,
              "ccy": "USD",
              "description": "Test debit",
              "tradingAccount": {
                "id": %d
              }
            }
            """,
            debitAmount,
            testAccount.getId()
        );

        mockMvc.perform(post("/api/ledger-entries").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());

        TradingAccount updated = tradingAccountRepository.findById(testAccount.getId()).orElseThrow();
        assertThat(updated.getBalance()).isEqualByComparingTo(initialBalance.subtract(debitAmount));
        assertThat(ledgerEntryRepository.count()).isEqualTo(beforeCount + 1);
    }

    @Test
    @Transactional
    void journalDebitAllowsNegativeBalance() throws Exception {
        // Edge case: negative balance allowed but flagged
        long beforeCount = ledgerEntryRepository.count();
        BigDecimal debitAmount = initialBalance.add(new BigDecimal("50.00")); // Will result in negative

        String body = String.format(
            """
            {
              "createdAt": "2024-01-15T10:00:00Z",
              "type": "DEBIT",
              "amount": %s,
              "ccy": "USD",
              "description": "Debit that results in negative balance",
              "tradingAccount": {
                "id": %d
              }
            }
            """,
            debitAmount,
            testAccount.getId()
        );

        mockMvc.perform(post("/api/ledger-entries").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());

        TradingAccount updated = tradingAccountRepository.findById(testAccount.getId()).orElseThrow();
        BigDecimal expectedBalance = initialBalance.subtract(debitAmount);
        assertThat(updated.getBalance()).isEqualByComparingTo(expectedBalance);
        assertThat(updated.getBalance().signum()).isNegative(); // Balance is negative
        assertThat(ledgerEntryRepository.count()).isEqualTo(beforeCount + 1);
    }

    @Test
    @Transactional
    void journalEntryPersistsCorrectly() throws Exception {
        BigDecimal creditAmount = new BigDecimal("25.00");

        String body = String.format(
            """
            {
              "createdAt": "2024-01-15T10:00:00Z",
              "type": "CREDIT",
              "amount": %s,
              "ccy": "USD",
              "description": "Persistent test entry",
              "tradingAccount": {
                "id": %d
              }
            }
            """,
            creditAmount,
            testAccount.getId()
        );

        mockMvc.perform(post("/api/ledger-entries").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());

        var entries = ledgerEntryRepository.findAll();
        assertThat(entries).isNotEmpty();
        var entry = entries
            .stream()
            .filter(e -> e.getDescription() != null && e.getDescription().contains("Persistent test entry"))
            .findFirst()
            .orElseThrow();
        assertThat(entry.getAmount()).isEqualByComparingTo(creditAmount);
        assertThat(entry.getTradingAccount().getId()).isEqualTo(testAccount.getId());
    }
}
