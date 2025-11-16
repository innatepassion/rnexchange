package com.rnexchange.web.rest;

import com.rnexchange.domain.Broker;
import com.rnexchange.domain.BrokerDesk;
import com.rnexchange.repository.BrokerDeskRepository;
import com.rnexchange.repository.LedgerEntryRepository;
import com.rnexchange.repository.OrderRepository;
import com.rnexchange.repository.PositionRepository;
import com.rnexchange.security.AuthoritiesConstants;
import com.rnexchange.security.SecurityUtils;
import com.rnexchange.service.dto.LedgerEntryDTO;
import com.rnexchange.service.dto.OrderDTO;
import com.rnexchange.service.dto.PositionDTO;
import com.rnexchange.service.mapper.LedgerEntryMapper;
import com.rnexchange.service.mapper.OrderMapper;
import com.rnexchange.service.mapper.PositionMapper;
import com.rnexchange.web.rest.errors.BadRequestAlertException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * T025: REST controller for Broker Admin portfolio views.
 *
 * Provides broker-scoped access to trading data:
 * - Orders for all traders under the broker
 * - Positions for all traders under the broker
 * - Ledger entries for all traders under the broker
 *
 * This resource enforces ROLE_BROKER_ADMIN and ensures admins can only see their own broker's data.
 */
@RestController
@RequestMapping("/api/admin/portfolio")
public class BrokerAdminPortfolioResource {

    private static final Logger LOG = LoggerFactory.getLogger(BrokerAdminPortfolioResource.class);

    private static final String ENTITY_NAME = "brokerAdminPortfolio";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final OrderRepository orderRepository;

    private final PositionRepository positionRepository;

    private final LedgerEntryRepository ledgerEntryRepository;

    private final BrokerDeskRepository brokerDeskRepository;

    private final OrderMapper orderMapper;

    private final PositionMapper positionMapper;

    private final LedgerEntryMapper ledgerEntryMapper;

    public BrokerAdminPortfolioResource(
        OrderRepository orderRepository,
        PositionRepository positionRepository,
        LedgerEntryRepository ledgerEntryRepository,
        BrokerDeskRepository brokerDeskRepository,
        OrderMapper orderMapper,
        PositionMapper positionMapper,
        LedgerEntryMapper ledgerEntryMapper
    ) {
        this.orderRepository = orderRepository;
        this.positionRepository = positionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.brokerDeskRepository = brokerDeskRepository;
        this.orderMapper = orderMapper;
        this.positionMapper = positionMapper;
        this.ledgerEntryMapper = ledgerEntryMapper;
    }

    /**
     * T025: GET /api/admin/portfolio/orders
     *
     * Returns all orders for traders under the current broker admin's broker.
     * Only accessible to users with ROLE_BROKER_ADMIN.
     *
     * @return list of orders filtered by broker
     */
    @GetMapping("/orders")
    public ResponseEntity<List<OrderDTO>> getBrokerOrders(Pageable pageable) {
        LOG.debug("REST request to get orders for broker admin");

        // Verify user has BROKER_ADMIN role
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.BROKER_ADMIN)) {
            throw new BadRequestAlertException("Access denied. Only BROKER_ADMIN can view portfolio data.", ENTITY_NAME, "accessdenied");
        }

        // Get current broker admin user login
        String adminLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new BadRequestAlertException("Unable to determine current broker admin", ENTITY_NAME, "nologin"));

        // Resolve broker from BrokerDesk
        BrokerDesk brokerDesk = brokerDeskRepository
            .findByUserLogin(adminLogin)
            .orElseThrow(() -> new BadRequestAlertException("BrokerDesk not found for current user", ENTITY_NAME, "nobrokdesk"));

        Broker broker = brokerDesk.getBroker();
        if (broker == null) {
            throw new BadRequestAlertException("Broker not associated with current user", ENTITY_NAME, "nobroker");
        }

        LOG.debug("Fetching orders for broker: {}", broker.getCode());

        // Fetch broker-scoped orders
        List<OrderDTO> orders = orderMapper.toDto(orderRepository.findByBrokerNonPaginated(broker));

        return ResponseEntity.ok(orders);
    }

    /**
     * T025: GET /api/admin/portfolio/positions
     *
     * Returns all positions for traders under the current broker admin's broker.
     * Only accessible to users with ROLE_BROKER_ADMIN.
     *
     * @return list of positions filtered by broker
     */
    @GetMapping("/positions")
    public ResponseEntity<List<PositionDTO>> getBrokerPositions(Pageable pageable) {
        LOG.debug("REST request to get positions for broker admin");

        // Verify user has BROKER_ADMIN role
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.BROKER_ADMIN)) {
            throw new BadRequestAlertException("Access denied. Only BROKER_ADMIN can view portfolio data.", ENTITY_NAME, "accessdenied");
        }

        // Get current broker admin user login
        String adminLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new BadRequestAlertException("Unable to determine current broker admin", ENTITY_NAME, "nologin"));

        // Resolve broker from BrokerDesk
        BrokerDesk brokerDesk = brokerDeskRepository
            .findByUserLogin(adminLogin)
            .orElseThrow(() -> new BadRequestAlertException("BrokerDesk not found for current user", ENTITY_NAME, "nobrokdesk"));

        Broker broker = brokerDesk.getBroker();
        if (broker == null) {
            throw new BadRequestAlertException("Broker not associated with current user", ENTITY_NAME, "nobroker");
        }

        LOG.debug("Fetching positions for broker: {}", broker.getCode());

        // Fetch broker-scoped positions
        List<PositionDTO> positions = positionMapper.toDto(positionRepository.findByBrokerNonPaginated(broker));

        return ResponseEntity.ok(positions);
    }

    /**
     * T025: GET /api/admin/portfolio/ledger-entries
     *
     * Returns all ledger entries for traders under the current broker admin's broker.
     * Only accessible to users with ROLE_BROKER_ADMIN.
     *
     * @return list of ledger entries filtered by broker
     */
    @GetMapping("/ledger-entries")
    public ResponseEntity<List<LedgerEntryDTO>> getBrokerLedgerEntries(Pageable pageable) {
        LOG.debug("REST request to get ledger entries for broker admin");

        // Verify user has BROKER_ADMIN role
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.BROKER_ADMIN)) {
            throw new BadRequestAlertException("Access denied. Only BROKER_ADMIN can view portfolio data.", ENTITY_NAME, "accessdenied");
        }

        // Get current broker admin user login
        String adminLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new BadRequestAlertException("Unable to determine current broker admin", ENTITY_NAME, "nologin"));

        // Resolve broker from BrokerDesk
        BrokerDesk brokerDesk = brokerDeskRepository
            .findByUserLogin(adminLogin)
            .orElseThrow(() -> new BadRequestAlertException("BrokerDesk not found for current user", ENTITY_NAME, "nobrokdesk"));

        Broker broker = brokerDesk.getBroker();
        if (broker == null) {
            throw new BadRequestAlertException("Broker not associated with current user", ENTITY_NAME, "nobroker");
        }

        LOG.debug("Fetching ledger entries for broker: {}", broker.getCode());

        // Fetch broker-scoped ledger entries
        List<LedgerEntryDTO> ledgerEntries = ledgerEntryMapper.toDto(ledgerEntryRepository.findByBrokerNonPaginated(broker));

        return ResponseEntity.ok(ledgerEntries);
    }
}
