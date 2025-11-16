package com.rnexchange.service.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Phase 2 (T009): Journal response payload.
 */
public class BrokerJournalResponseDTO implements Serializable {

    private Long ledgerEntryId;
    private BigDecimal balanceAfter;
    private String idempotencyToken;

    public Long getLedgerEntryId() {
        return ledgerEntryId;
    }

    public void setLedgerEntryId(Long ledgerEntryId) {
        this.ledgerEntryId = ledgerEntryId;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getIdempotencyToken() {
        return idempotencyToken;
    }

    public void setIdempotencyToken(String idempotencyToken) {
        this.idempotencyToken = idempotencyToken;
    }
}
