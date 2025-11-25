package com.rnexchange.service.broker;

import com.rnexchange.domain.Broker;
import com.rnexchange.domain.IdempotencyToken;
import com.rnexchange.domain.LedgerEntry;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.domain.enumeration.LedgerEntryType;
import com.rnexchange.repository.BrokerRepository;
import com.rnexchange.repository.IdempotencyTokenRepository;
import com.rnexchange.repository.LedgerEntryRepository;
import com.rnexchange.repository.TradingAccountRepository;
import com.rnexchange.service.dto.broker.JournalRequestDTO;
import com.rnexchange.service.dto.broker.JournalResultDTO;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrokerJournalService {

    private static final Logger log = LoggerFactory.getLogger(BrokerJournalService.class);
    private static final String CORRELATION_ID_KEY = "correlationId";

    private final IdempotencyTokenRepository idempotencyTokenRepository;
    private final TradingAccountRepository tradingAccountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final BrokerRepository brokerRepository;

    public BrokerJournalService(
        IdempotencyTokenRepository idempotencyTokenRepository,
        TradingAccountRepository tradingAccountRepository,
        LedgerEntryRepository ledgerEntryRepository,
        BrokerRepository brokerRepository
    ) {
        this.idempotencyTokenRepository = idempotencyTokenRepository;
        this.tradingAccountRepository = tradingAccountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.brokerRepository = brokerRepository;
    }

    @Transactional
    public JournalResultDTO applyJournal(Long tradingAccountId, String idempotencyKey, JournalRequestDTO request, Long brokerId) {
        String correlationId = MDC.get(CORRELATION_ID_KEY);
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
            MDC.put(CORRELATION_ID_KEY, correlationId);
        }
        try {
            log.info(
                "[correlationId={}] [flow=broker_funds_journal] Journal request received: direction={}, amount={}, reason='{}', brokerId={}, tradingAccountId={}, idempotencyKey={}",
                correlationId,
                request.getDirection(),
                request.getAmount(),
                request.getReason(),
                brokerId,
                tradingAccountId,
                idempotencyKey
            );
            Optional<IdempotencyToken> existing = idempotencyTokenRepository.findByToken(idempotencyKey);
            if (existing.isPresent()) {
                IdempotencyToken existingToken = existing.orElseThrow();
                if (existingToken.getLedgerEntry() != null) {
                    log.info(
                        "[correlationId={}] [flow=broker_funds_journal] [outcome=idempotent_replay] Idempotent replay detected, returning original result: brokerId={}, tradingAccountId={}, idempotencyKey={}",
                        correlationId,
                        brokerId,
                        tradingAccountId,
                        idempotencyKey
                    );
                    return toResult(existingToken);
                }
            }

            // Find the trading account by ID
            TradingAccount account = tradingAccountRepository
                .findById(tradingAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Trading account not found: " + tradingAccountId));

            // Verify broker scope if brokerId is provided
            if (brokerId != null && account.getBroker() != null && !brokerId.equals(account.getBroker().getId())) {
                throw new IllegalArgumentException("Trading account " + tradingAccountId + " does not belong to broker " + brokerId);
            }

            BigDecimal amount = request.getAmount();
            boolean isCredit = "credit".equalsIgnoreCase(request.getDirection());
            BigDecimal newBalance = isCredit ? account.getBalance().add(amount) : account.getBalance().subtract(amount);
            account.setBalance(newBalance);
            tradingAccountRepository.save(account);

            LedgerEntry entry = new LedgerEntry()
                .createdAt(Instant.now())
                .type(isCredit ? LedgerEntryType.CREDIT : LedgerEntryType.DEBIT)
                .amount(amount)
                .ccy(account.getBaseCcy())
                .balanceAfter(newBalance)
                .description(request.getReason())
                .tradingAccount(account);
            entry = ledgerEntryRepository.save(entry);

            IdempotencyToken token = existing.orElseGet(IdempotencyToken::new);
            token.setCreatedAt(token.getCreatedAt() == null ? Instant.now() : token.getCreatedAt());
            token.setToken(idempotencyKey);
            token.setLedgerEntry(entry);
            token.setTradingAccount(account);
            token.setBroker(account.getBroker());
            idempotencyTokenRepository.save(token);

            log.info(
                "[correlationId={}] [flow=broker_funds_journal] [outcome=success] Journal applied successfully: direction={}, amount={}, ledgerEntryId={}, brokerId={}, tradingAccountId={}, idempotencyKey={}, newBalance={}",
                correlationId,
                request.getDirection(),
                amount,
                entry.getId(),
                token.getBroker() != null ? token.getBroker().getId() : null,
                account.getId(),
                idempotencyKey,
                newBalance
            );
            return toResult(token);
        } catch (Exception e) {
            log.error(
                "[correlationId={}] [flow=broker_funds_journal] [outcome=error] Error applying journal: {}",
                correlationId,
                e.getMessage(),
                e
            );
            throw e;
        } finally {
            // Only remove if we created it
            if (MDC.get(CORRELATION_ID_KEY) != null && MDC.get(CORRELATION_ID_KEY).equals(correlationId)) {
                MDC.remove(CORRELATION_ID_KEY);
            }
        }
    }

    private JournalResultDTO toResult(IdempotencyToken token) {
        LedgerEntry le = token.getLedgerEntry();
        JournalResultDTO.LedgerEntry ledger = new JournalResultDTO.LedgerEntry();
        ledger.setId(UUID.randomUUID());
        ledger.setType(("JOURNAL_" + le.getType().name()));
        ledger.setAmount(le.getAmount());
        ledger.setReason(le.getDescription());
        ledger.setCreatedAt(le.getCreatedAt());
        ledger.setCreatedByUserId(UUID.randomUUID());

        JournalResultDTO.Account account = new JournalResultDTO.Account();
        account.setTradingAccountId(UUID.randomUUID());
        account.setCash(le.getBalanceAfter());

        JournalResultDTO result = new JournalResultDTO();
        result.setLedgerEntry(ledger);
        result.setAccount(account);
        return result;
    }
}
