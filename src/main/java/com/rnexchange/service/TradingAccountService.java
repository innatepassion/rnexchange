package com.rnexchange.service;

import com.rnexchange.domain.TradingAccount;
import com.rnexchange.repository.TradingAccountRepository;
import com.rnexchange.service.dto.TradingAccountDTO;
import com.rnexchange.service.mapper.TradingAccountMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.rnexchange.domain.TradingAccount}.
 */
@Service
@Transactional
public class TradingAccountService {

    private static final Logger LOG = LoggerFactory.getLogger(TradingAccountService.class);

    private final TradingAccountRepository tradingAccountRepository;

    private final TradingAccountMapper tradingAccountMapper;

    public TradingAccountService(TradingAccountRepository tradingAccountRepository, TradingAccountMapper tradingAccountMapper) {
        this.tradingAccountRepository = tradingAccountRepository;
        this.tradingAccountMapper = tradingAccountMapper;
    }

    /**
     * Save a tradingAccount.
     *
     * @param tradingAccountDTO the entity to save.
     * @return the persisted entity.
     */
    public TradingAccountDTO save(TradingAccountDTO tradingAccountDTO) {
        LOG.debug("Request to save TradingAccount : {}", tradingAccountDTO);
        TradingAccount tradingAccount = tradingAccountMapper.toEntity(tradingAccountDTO);
        tradingAccount = tradingAccountRepository.save(tradingAccount);
        return tradingAccountMapper.toDto(tradingAccount);
    }

    /**
     * Update a tradingAccount.
     *
     * @param tradingAccountDTO the entity to save.
     * @return the persisted entity.
     */
    public TradingAccountDTO update(TradingAccountDTO tradingAccountDTO) {
        LOG.debug("Request to update TradingAccount : {}", tradingAccountDTO);
        TradingAccount tradingAccount = tradingAccountMapper.toEntity(tradingAccountDTO);
        tradingAccount = tradingAccountRepository.save(tradingAccount);
        return tradingAccountMapper.toDto(tradingAccount);
    }

    /**
     * Partially update a tradingAccount.
     *
     * @param tradingAccountDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<TradingAccountDTO> partialUpdate(TradingAccountDTO tradingAccountDTO) {
        LOG.debug("Request to partially update TradingAccount : {}", tradingAccountDTO);

        return tradingAccountRepository
            .findById(tradingAccountDTO.getId())
            .map(existingTradingAccount -> {
                tradingAccountMapper.partialUpdate(existingTradingAccount, tradingAccountDTO);

                return existingTradingAccount;
            })
            .map(tradingAccountRepository::save)
            .map(tradingAccountMapper::toDto);
    }

    /**
     * Get all the tradingAccounts with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<TradingAccountDTO> findAllWithEagerRelationships(Pageable pageable) {
        return tradingAccountRepository.findAllWithEagerRelationships(pageable).map(tradingAccountMapper::toDto);
    }

    /**
     * Get one tradingAccount by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<TradingAccountDTO> findOne(Long id) {
        LOG.debug("Request to get TradingAccount : {}", id);
        return tradingAccountRepository.findOneWithEagerRelationships(id).map(tradingAccountMapper::toDto);
    }

    /**
     * Delete the tradingAccount by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete TradingAccount : {}", id);
        tradingAccountRepository.deleteById(id);
    }
}
