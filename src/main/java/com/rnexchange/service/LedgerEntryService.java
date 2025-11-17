package com.rnexchange.service;

import com.rnexchange.domain.LedgerEntry;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.domain.enumeration.LedgerEntryType;
import com.rnexchange.repository.LedgerEntryRepository;
import com.rnexchange.repository.TradingAccountRepository;
import com.rnexchange.service.dto.LedgerEntryDTO;
import com.rnexchange.service.mapper.LedgerEntryMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.rnexchange.domain.LedgerEntry}.
 * M6 User Story 2: Enhanced to update TradingAccount balance when journal entries (CREDIT/DEBIT) are created,
 * allowing negative balances as per FR-004.
 */
@Service
@Transactional
public class LedgerEntryService {

    private static final Logger LOG = LoggerFactory.getLogger(LedgerEntryService.class);

    private final LedgerEntryRepository ledgerEntryRepository;

    private final LedgerEntryMapper ledgerEntryMapper;

    private final TradingAccountRepository tradingAccountRepository;

    public LedgerEntryService(
        LedgerEntryRepository ledgerEntryRepository,
        LedgerEntryMapper ledgerEntryMapper,
        TradingAccountRepository tradingAccountRepository
    ) {
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.ledgerEntryMapper = ledgerEntryMapper;
        this.tradingAccountRepository = tradingAccountRepository;
    }

    /**
     * Save a ledgerEntry.
     * M6 User Story 2: Updates TradingAccount balance for CREDIT/DEBIT entries, allowing negative balances.
     *
     * @param ledgerEntryDTO the entity to save.
     * @return the persisted entity.
     */
    public LedgerEntryDTO save(LedgerEntryDTO ledgerEntryDTO) {
        LOG.debug("Request to save LedgerEntry : {}", ledgerEntryDTO);
        LedgerEntry ledgerEntry = ledgerEntryMapper.toEntity(ledgerEntryDTO);

        // M6 User Story 2: Update account balance for journal entries (CREDIT/DEBIT)
        if (ledgerEntry.getTradingAccount() != null && ledgerEntry.getTradingAccount().getId() != null) {
            TradingAccount account = tradingAccountRepository
                .findById(ledgerEntry.getTradingAccount().getId())
                .orElseThrow(() -> new IllegalArgumentException("TradingAccount not found"));

            if (ledgerEntry.getType() == LedgerEntryType.CREDIT || ledgerEntry.getType() == LedgerEntryType.DEBIT) {
                BigDecimal currentBalance = account.getBalance();
                BigDecimal entryAmount = ledgerEntry.getAmount();
                BigDecimal newBalance;

                if (ledgerEntry.getType() == LedgerEntryType.CREDIT) {
                    newBalance = currentBalance.add(entryAmount);
                } else {
                    newBalance = currentBalance.subtract(entryAmount);
                    // Negative balance is allowed per FR-004
                }

                account.setBalance(newBalance);
                tradingAccountRepository.save(account);

                // Set balanceAfter on the ledger entry
                if (ledgerEntry.getCreatedAt() == null) {
                    ledgerEntry.setCreatedAt(Instant.now());
                }
                ledgerEntry.setBalanceAfter(newBalance);

                LOG.info(
                    "Updated TradingAccount balance: accountId={}, type={}, amount={}, oldBalance={}, newBalance={}",
                    account.getId(),
                    ledgerEntry.getType(),
                    entryAmount,
                    currentBalance,
                    newBalance
                );
            }
        }

        ledgerEntry = ledgerEntryRepository.save(ledgerEntry);
        return ledgerEntryMapper.toDto(ledgerEntry);
    }

    /**
     * Update a ledgerEntry.
     *
     * @param ledgerEntryDTO the entity to save.
     * @return the persisted entity.
     */
    public LedgerEntryDTO update(LedgerEntryDTO ledgerEntryDTO) {
        LOG.debug("Request to update LedgerEntry : {}", ledgerEntryDTO);
        LedgerEntry ledgerEntry = ledgerEntryMapper.toEntity(ledgerEntryDTO);
        ledgerEntry = ledgerEntryRepository.save(ledgerEntry);
        return ledgerEntryMapper.toDto(ledgerEntry);
    }

    /**
     * Partially update a ledgerEntry.
     *
     * @param ledgerEntryDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<LedgerEntryDTO> partialUpdate(LedgerEntryDTO ledgerEntryDTO) {
        LOG.debug("Request to partially update LedgerEntry : {}", ledgerEntryDTO);

        return ledgerEntryRepository
            .findById(ledgerEntryDTO.getId())
            .map(existingLedgerEntry -> {
                ledgerEntryMapper.partialUpdate(existingLedgerEntry, ledgerEntryDTO);

                return existingLedgerEntry;
            })
            .map(ledgerEntryRepository::save)
            .map(ledgerEntryMapper::toDto);
    }

    /**
     * Get one ledgerEntry by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<LedgerEntryDTO> findOne(Long id) {
        LOG.debug("Request to get LedgerEntry : {}", id);
        return ledgerEntryRepository.findById(id).map(ledgerEntryMapper::toDto);
    }

    /**
     * Delete the ledgerEntry by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete LedgerEntry : {}", id);
        ledgerEntryRepository.deleteById(id);
    }
}
