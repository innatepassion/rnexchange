package com.rnexchange.service;

import com.rnexchange.domain.Instrument;
import com.rnexchange.domain.Order;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.repository.InstrumentRepository;
import com.rnexchange.repository.OrderRepository;
import com.rnexchange.repository.TradingAccountRepository;
import com.rnexchange.security.AuthoritiesConstants;
import com.rnexchange.service.dto.MarginAssessment;
import com.rnexchange.service.dto.OrderDTO;
import com.rnexchange.service.dto.TraderOrderRequest;
import com.rnexchange.service.dto.TraderOrderResult;
import com.rnexchange.service.mapper.OrderMapper;
import com.rnexchange.service.trading.TraderAuditStructuredLogger;
import com.rnexchange.service.trading.TraderAuditStructuredLogger.TraderAuditPayload;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.rnexchange.domain.Order}.
 */
@Service
@Transactional
public class OrderService {

    private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);
    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("com.rnexchange.service.trading.TraderAuditLogger");

    private final OrderRepository orderRepository;

    private final OrderMapper orderMapper;

    private final InstrumentRepository instrumentRepository;

    private final TradingAccountRepository tradingAccountRepository;

    private final MarginService marginService;

    private final TraderAuditStructuredLogger traderAuditStructuredLogger;

    public OrderService(
        OrderRepository orderRepository,
        OrderMapper orderMapper,
        InstrumentRepository instrumentRepository,
        TradingAccountRepository tradingAccountRepository,
        MarginService marginService,
        TraderAuditStructuredLogger traderAuditStructuredLogger
    ) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.instrumentRepository = instrumentRepository;
        this.tradingAccountRepository = tradingAccountRepository;
        this.marginService = marginService;
        this.traderAuditStructuredLogger = traderAuditStructuredLogger;
    }

    /**
     * Save a order.
     *
     * @param orderDTO the entity to save.
     * @return the persisted entity.
     */
    public OrderDTO save(OrderDTO orderDTO) {
        LOG.debug("Request to save Order : {}", orderDTO);
        Order order = orderMapper.toEntity(orderDTO);
        order = orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    /**
     * Update a order.
     *
     * @param orderDTO the entity to save.
     * @return the persisted entity.
     */
    public OrderDTO update(OrderDTO orderDTO) {
        LOG.debug("Request to update Order : {}", orderDTO);
        Order order = orderMapper.toEntity(orderDTO);
        order = orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    /**
     * Partially update a order.
     *
     * @param orderDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<OrderDTO> partialUpdate(OrderDTO orderDTO) {
        LOG.debug("Request to partially update Order : {}", orderDTO);

        return orderRepository
            .findById(orderDTO.getId())
            .map(existingOrder -> {
                orderMapper.partialUpdate(existingOrder, orderDTO);

                return existingOrder;
            })
            .map(orderRepository::save)
            .map(orderMapper::toDto);
    }

    /**
     * Get all the orders with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<OrderDTO> findAllWithEagerRelationships(Pageable pageable) {
        return orderRepository.findAllWithEagerRelationships(pageable).map(orderMapper::toDto);
    }

    /**
     * Get one order by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<OrderDTO> findOne(Long id) {
        LOG.debug("Request to get Order : {}", id);
        return orderRepository.findOneWithEagerRelationships(id).map(orderMapper::toDto);
    }

    /**
     * Delete the order by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Order : {}", id);
        orderRepository.deleteById(id);
    }

    public TraderOrderResult submitTraderOrder(TraderOrderRequest request) {
        Objects.requireNonNull(request, "Trader order request must not be null");
        TradingAccount tradingAccount = resolveTradingAccount(request.getTraderLogin());
        Instrument instrument = resolveInstrument(request);
        validateTickAndLotSizes(request, instrument);

        MarginAssessment assessment;
        try {
            assessment = marginService.evaluateMargin(request);
        } catch (InsufficientMarginException ex) {
            logAudit(tradingAccount, instrument, "REJECTED", ex.getMessage(), ex.getAssessment(), request);
            throw ex;
        }

        Order order = new Order()
            .side(request.getSide())
            .type(request.getType())
            .qty(request.getQuantity().setScale(2, RoundingMode.HALF_UP))
            .limitPx(request.getPrice().setScale(2, RoundingMode.HALF_UP))
            .tif(request.getTif())
            .status(com.rnexchange.domain.enumeration.OrderStatus.ACCEPTED)
            .venue(instrument.getExchangeCode())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .tradingAccount(tradingAccount)
            .instrument(instrument);

        order = orderRepository.save(order);
        OrderDTO dto = orderMapper.toDto(order);

        logAudit(tradingAccount, instrument, "ACCEPTED", "ACCEPTED", assessment, request);

        return new TraderOrderResult(dto, assessment);
    }

    private TradingAccount resolveTradingAccount(String traderLogin) {
        return tradingAccountRepository
            .findFirstByTrader_User_LoginOrderByIdAsc(traderLogin)
            .orElseThrow(() -> new IllegalStateException("Trading account not found for trader login " + traderLogin));
    }

    private Instrument resolveInstrument(TraderOrderRequest request) {
        if (request.getInstrumentSymbol() != null) {
            // Avoid Optional.get() to satisfy modernizer rules while maintaining existing behaviour.
            Optional<Instrument> bySymbol = instrumentRepository.findOneBySymbol(request.getInstrumentSymbol());
            if (bySymbol.isPresent()) {
                return bySymbol.orElseThrow();
            }
        }
        if (request.getInstrumentId() != null) {
            return instrumentRepository
                .findById(request.getInstrumentId())
                .orElseThrow(() ->
                    new IllegalStateException(
                        "Instrument not found for id " + request.getInstrumentId() + " (symbol=" + request.getInstrumentSymbol() + ")"
                    )
                );
        }
        throw new IllegalStateException("Instrument not found for symbol " + request.getInstrumentSymbol());
    }

    private void validateTickAndLotSizes(TraderOrderRequest request, Instrument instrument) {
        BigDecimal lotSize = BigDecimal.valueOf(instrument.getLotSize() != null ? instrument.getLotSize() : 1L);
        if (lotSize.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal[] division = request.getQuantity().divideAndRemainder(lotSize);
            if (division[1].compareTo(BigDecimal.ZERO) != 0) {
                throw new IllegalArgumentException(
                    "Quantity %s must align with lot size %s for instrument %s".formatted(
                            request.getQuantity(),
                            lotSize.toPlainString(),
                            instrument.getSymbol()
                        )
                );
            }
        }

        BigDecimal tickSize = instrument.getTickSize();
        if (tickSize != null && tickSize.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal scaledPrice = request.getPrice().setScale(Math.max(tickSize.scale(), 2), RoundingMode.HALF_UP);
            BigDecimal remainder = scaledPrice.remainder(tickSize);
            if (remainder.compareTo(BigDecimal.ZERO) != 0) {
                throw new IllegalArgumentException(
                    "Price %s must align with tick size %s for instrument %s".formatted(
                            scaledPrice.toPlainString(),
                            tickSize.toPlainString(),
                            instrument.getSymbol()
                        )
                );
            }
        }
    }

    private void logAudit(
        TradingAccount tradingAccount,
        Instrument instrument,
        String status,
        String outcome,
        MarginAssessment assessment,
        TraderOrderRequest request
    ) {
        String actorLogin = request.getTraderLogin();
        if (tradingAccount.getTrader() != null && tradingAccount.getTrader().getUser() != null) {
            actorLogin = tradingAccount.getTrader().getUser().getLogin();
        }
        String actorRole = AuthoritiesConstants.TRADER.replace("ROLE_", "");
        TraderAuditPayload payload = new TraderAuditPayload(
            actorLogin,
            actorRole,
            instrument.getSymbol(),
            status,
            outcome,
            assessment,
            request.getQuantity(),
            request.getPrice()
        );
        AUDIT_LOG.info(traderAuditStructuredLogger.build(payload));
    }
}
