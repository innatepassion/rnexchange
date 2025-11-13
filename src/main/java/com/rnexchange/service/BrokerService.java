package com.rnexchange.service;

import com.rnexchange.domain.Broker;
import com.rnexchange.repository.BrokerDeskRepository;
import com.rnexchange.repository.BrokerRepository;
import com.rnexchange.repository.projection.BrokerInstrumentRow;
import com.rnexchange.service.dto.BrokerBaselineDTO;
import com.rnexchange.service.dto.BrokerDTO;
import com.rnexchange.service.dto.BrokerInstrumentDTO;
import com.rnexchange.service.mapper.BrokerMapper;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.rnexchange.domain.Broker}.
 */
@Service
@Transactional
public class BrokerService {

    private static final Logger LOG = LoggerFactory.getLogger(BrokerService.class);

    private final BrokerRepository brokerRepository;

    private final BrokerDeskRepository brokerDeskRepository;

    private final BrokerMapper brokerMapper;

    public BrokerService(BrokerRepository brokerRepository, BrokerDeskRepository brokerDeskRepository, BrokerMapper brokerMapper) {
        this.brokerRepository = brokerRepository;
        this.brokerDeskRepository = brokerDeskRepository;
        this.brokerMapper = brokerMapper;
    }

    /**
     * Save a broker.
     *
     * @param brokerDTO the entity to save.
     * @return the persisted entity.
     */
    public BrokerDTO save(BrokerDTO brokerDTO) {
        LOG.debug("Request to save Broker : {}", brokerDTO);
        Broker broker = brokerMapper.toEntity(brokerDTO);
        broker = brokerRepository.save(broker);
        return brokerMapper.toDto(broker);
    }

    /**
     * Update a broker.
     *
     * @param brokerDTO the entity to save.
     * @return the persisted entity.
     */
    public BrokerDTO update(BrokerDTO brokerDTO) {
        LOG.debug("Request to update Broker : {}", brokerDTO);
        Broker broker = brokerMapper.toEntity(brokerDTO);
        broker = brokerRepository.save(broker);
        return brokerMapper.toDto(broker);
    }

    /**
     * Partially update a broker.
     *
     * @param brokerDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<BrokerDTO> partialUpdate(BrokerDTO brokerDTO) {
        LOG.debug("Request to partially update Broker : {}", brokerDTO);

        return brokerRepository
            .findById(brokerDTO.getId())
            .map(existingBroker -> {
                brokerMapper.partialUpdate(existingBroker, brokerDTO);

                return existingBroker;
            })
            .map(brokerRepository::save)
            .map(brokerMapper::toDto);
    }

    /**
     * Get all the brokers with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<BrokerDTO> findAllWithEagerRelationships(Pageable pageable) {
        return brokerRepository.findAllWithEagerRelationships(pageable).map(brokerMapper::toDto);
    }

    /**
     * Get one broker by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<BrokerDTO> findOne(Long id) {
        LOG.debug("Request to get Broker : {}", id);
        return brokerRepository.findOneWithEagerRelationships(id).map(brokerMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<BrokerBaselineDTO> findBaseline(Long id) {
        LOG.debug("Request to get Broker baseline view : {}", id);
        return brokerRepository
            .findOneWithEagerRelationships(id)
            .map(broker -> {
                List<BrokerInstrumentRow> instrumentRows = brokerRepository.findBaselineInstrumentCatalog(id);
                List<BrokerInstrumentDTO> instrumentCatalog = instrumentRows
                    .stream()
                    .map(row ->
                        new BrokerInstrumentDTO(
                            row.symbol(),
                            row.name(),
                            row.exchangeCode(),
                            row.assetClass(),
                            row.tickSize(),
                            row.lotSize(),
                            row.currency()
                        )
                    )
                    .toList();

                Set<String> exchangeMemberships = new LinkedHashSet<>();
                if (broker.getExchange() != null && broker.getExchange().getCode() != null) {
                    exchangeMemberships.add(broker.getExchange().getCode());
                }
                instrumentRows
                    .stream()
                    .map(BrokerInstrumentRow::exchangeCode)
                    .forEach(code -> {
                        if (code != null) {
                            exchangeMemberships.add(code);
                        }
                    });

                List<String> deskLogins = brokerDeskRepository.findUserLoginsForBroker(id);

                BrokerBaselineDTO dto = new BrokerBaselineDTO();
                dto.setId(broker.getId());
                dto.setCode(broker.getCode());
                dto.setName(broker.getName());
                dto.setStatus(broker.getStatus());
                if (broker.getExchange() != null) {
                    dto.setExchangeCode(broker.getExchange().getCode());
                    dto.setExchangeName(broker.getExchange().getName());
                    dto.setExchangeTimezone(broker.getExchange().getTimezone());
                }
                dto.setBrokerAdminLogin(deskLogins.stream().findFirst().orElse(null));
                dto.setInstrumentCatalog(instrumentCatalog);
                dto.setInstrumentCount(instrumentCatalog.size());
                dto.setExchangeMemberships(List.copyOf(exchangeMemberships));
                return dto;
            });
    }

    /**
     * Delete the broker by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Broker : {}", id);
        brokerRepository.deleteById(id);
    }
}
