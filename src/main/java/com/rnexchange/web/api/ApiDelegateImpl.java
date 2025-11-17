package com.rnexchange.web.api;

import com.rnexchange.service.api.dto.JournalRequest;
import com.rnexchange.service.api.dto.JournalResult;
import com.rnexchange.service.api.dto.JournalResultAccount;
import com.rnexchange.service.api.dto.LedgerEntry;
import com.rnexchange.service.api.dto.TraderDetails;
import com.rnexchange.service.api.dto.TraderPage;
import com.rnexchange.service.broker.BrokerJournalService;
import com.rnexchange.service.broker.BrokerTradersService;
import com.rnexchange.service.dto.broker.JournalRequestDTO;
import com.rnexchange.service.dto.broker.JournalResultDTO;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ApiDelegateImpl implements ApiApiDelegate {

    private final BrokerTradersService brokerTradersService;
    private final BrokerJournalService brokerJournalService;

    public ApiDelegateImpl(BrokerTradersService brokerTradersService, BrokerJournalService brokerJournalService) {
        this.brokerTradersService = brokerTradersService;
        this.brokerJournalService = brokerJournalService;
    }

    @Override
    public ResponseEntity<TraderPage> listBrokerTraders(Integer page, Integer size) {
        TraderPage resp = brokerTradersService.listTradersApi(page == null ? 0 : page, size == null ? 20 : size);
        return ResponseEntity.ok(resp);
    }

    @Override
    public ResponseEntity<TraderDetails> getBrokerTrader(UUID traderId) {
        TraderDetails details = brokerTradersService.getTraderDetailsApi(traderId);
        return ResponseEntity.ok(details);
    }

    @Override
    public ResponseEntity<JournalResult> createJournalEntry(UUID tradingAccountId, String idempotencyKey, JournalRequest journalRequest) {
        JournalRequestDTO req = new JournalRequestDTO();
        req.setDirection(journalRequest.getDirection().getValue());
        req.setAmount(java.math.BigDecimal.valueOf(journalRequest.getAmount()));
        req.setReason(journalRequest.getReason());
        // Convert UUID to Long: The OpenAPI contract uses UUID but the DB uses Long.
        // If the UUID string is actually a numeric string (like "1650"), parse it directly.
        // Otherwise, use a hash of the UUID to get a Long value.
        Long accountId;
        String uuidStr = tradingAccountId.toString();
        try {
            // Try parsing as Long directly (in case it's actually a numeric string like "1650")
            accountId = Long.parseLong(uuidStr);
        } catch (NumberFormatException e) {
            // If it's a real UUID, use a hash to convert to Long
            // Use the most significant bits, but ensure it's positive
            long msb = tradingAccountId.getMostSignificantBits();
            accountId = (msb < 0) ? -(msb + 1) : msb;
            // Ensure it's within reasonable range for a Long ID
            accountId = Math.abs(accountId) % Long.MAX_VALUE;
        }
        JournalResultDTO result = brokerJournalService.applyJournal(accountId, idempotencyKey, req, null);

        LedgerEntry le = new LedgerEntry()
            .type(LedgerEntry.TypeEnum.fromValue(result.getLedgerEntry().getType()))
            .amount(result.getLedgerEntry().getAmount().doubleValue())
            .reason(result.getLedgerEntry().getReason())
            .createdAt(java.time.OffsetDateTime.ofInstant(result.getLedgerEntry().getCreatedAt(), java.time.ZoneOffset.UTC))
            .createdByUserId(result.getLedgerEntry().getCreatedByUserId());

        JournalResultAccount acc = new JournalResultAccount()
            .tradingAccountId(result.getAccount().getTradingAccountId())
            .cash(result.getAccount().getCash().doubleValue());

        JournalResult out = new JournalResult().ledgerEntry(le).account(acc);
        return ResponseEntity.ok(out);
    }
}
