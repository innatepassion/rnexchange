package com.rnexchange.service.dto.broker;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class JournalResultDTO {

    private LedgerEntry ledgerEntry;
    private Account account;

    public LedgerEntry getLedgerEntry() {
        return ledgerEntry;
    }

    public void setLedgerEntry(LedgerEntry ledgerEntry) {
        this.ledgerEntry = ledgerEntry;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public static class LedgerEntry {

        private UUID id;
        private String type;
        private BigDecimal amount;
        private String reason;
        private Instant createdAt;
        private UUID createdByUserId;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }

        public UUID getCreatedByUserId() {
            return createdByUserId;
        }

        public void setCreatedByUserId(UUID createdByUserId) {
            this.createdByUserId = createdByUserId;
        }
    }

    public static class Account {

        private UUID tradingAccountId;
        private BigDecimal cash;

        public UUID getTradingAccountId() {
            return tradingAccountId;
        }

        public void setTradingAccountId(UUID tradingAccountId) {
            this.tradingAccountId = tradingAccountId;
        }

        public BigDecimal getCash() {
            return cash;
        }

        public void setCash(BigDecimal cash) {
            this.cash = cash;
        }
    }
}
