package com.rnexchange.service;

import com.rnexchange.domain.Execution;
import com.rnexchange.domain.Order;
import com.rnexchange.service.dto.ExecutionDTO;
import com.rnexchange.service.dto.OrderDTO;
import com.rnexchange.service.mapper.ExecutionMapper;
import com.rnexchange.service.mapper.OrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * T014: Service for publishing WebSocket notifications for trading events.
 *
 * Publishes order and execution updates to:
 * - `/topic/orders/{tradingAccountId}` - order status changes
 * - `/topic/executions/{tradingAccountId}` - execution records
 *
 * These notifications enable real-time UI updates without page reload per FR-012 (< 2 seconds latency 95% of time).
 */
@Service
public class TradingWebSocketService {

    private static final Logger LOG = LoggerFactory.getLogger(TradingWebSocketService.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final OrderMapper orderMapper;
    private final ExecutionMapper executionMapper;

    public TradingWebSocketService(SimpMessagingTemplate messagingTemplate, OrderMapper orderMapper, ExecutionMapper executionMapper) {
        this.messagingTemplate = messagingTemplate;
        this.orderMapper = orderMapper;
        this.executionMapper = executionMapper;
    }

    /**
     * Publish order status change to the trading account's subscribers.
     * Called when:
     * - Order is created
     * - Order status changes (NEW -> ACCEPTED -> FILLED/REJECTED)
     *
     * Topic: `/topic/orders/{tradingAccountId}`
     *
     * @param order the order with updated status
     */
    public void publishOrderNotification(Order order) {
        if (order == null || order.getTradingAccount() == null) {
            LOG.warn("Cannot publish order notification: invalid order or trading account");
            return;
        }

        try {
            Long tradingAccountId = order.getTradingAccount().getId();
            OrderDTO orderDTO = orderMapper.toDto(order);

            String destination = "/topic/orders/" + tradingAccountId;
            messagingTemplate.convertAndSend(destination, orderDTO);

            LOG.debug("Published order notification to {}: order {} status {}", destination, order.getId(), order.getStatus());
        } catch (Exception e) {
            LOG.error("Error publishing order notification for order {}", order.getId(), e);
        }
    }

    /**
     * Publish execution record to the trading account's subscribers.
     * Called immediately after an order is filled (execution record created).
     *
     * Topic: `/topic/executions/{tradingAccountId}`
     *
     * @param execution the execution record
     */
    public void publishExecutionNotification(Execution execution) {
        if (execution == null || execution.getTradingAccount() == null) {
            LOG.warn("Cannot publish execution notification: invalid execution or trading account");
            return;
        }

        try {
            Long tradingAccountId = execution.getTradingAccount().getId();
            ExecutionDTO executionDTO = executionMapper.toDto(execution);

            String destination = "/topic/executions/" + tradingAccountId;
            messagingTemplate.convertAndSend(destination, executionDTO);

            LOG.debug(
                "Published execution notification to {}: execution {} for order {}",
                destination,
                execution.getId(),
                execution.getOrder().getId()
            );
        } catch (Exception e) {
            LOG.error("Error publishing execution notification for execution {}", execution.getId(), e);
        }
    }

    /**
     * Publish combined trade event (order + execution) for real-time UI synchronization.
     * This can be used for coordinated updates to avoid multiple messages.
     *
     * @param order the completed order
     * @param execution the execution record
     */
    public void publishTradeCompletedNotification(Order order, Execution execution) {
        if (order == null || execution == null) {
            LOG.warn("Cannot publish trade completed notification: invalid order or execution");
            return;
        }

        // Publish both order and execution
        publishOrderNotification(order);
        publishExecutionNotification(execution);
    }

    /**
     * Publish position update notification.
     * Could be used to notify about position changes in Portfolio view.
     *
     * @param tradingAccountId the trading account ID
     * @param positionUpdate a map or DTO with position changes
     */
    public void publishPositionUpdateNotification(Long tradingAccountId, Object positionUpdate) {
        if (tradingAccountId == null) {
            LOG.warn("Cannot publish position update: invalid trading account ID");
            return;
        }

        try {
            String destination = "/topic/positions/" + tradingAccountId;
            messagingTemplate.convertAndSend(destination, positionUpdate);

            LOG.debug("Published position update notification to {}", destination);
        } catch (Exception e) {
            LOG.error("Error publishing position update notification for account {}", tradingAccountId, e);
        }
    }

    /**
     * Publish ledger entry notification for cash balance updates.
     * Could be used to notify about balance changes.
     *
     * @param tradingAccountId the trading account ID
     * @param ledgerUpdate a DTO with ledger entry details
     */
    public void publishLedgerUpdateNotification(Long tradingAccountId, Object ledgerUpdate) {
        if (tradingAccountId == null) {
            LOG.warn("Cannot publish ledger update: invalid trading account ID");
            return;
        }

        try {
            String destination = "/topic/ledger/" + tradingAccountId;
            messagingTemplate.convertAndSend(destination, ledgerUpdate);

            LOG.debug("Published ledger update notification to {}", destination);
        } catch (Exception e) {
            LOG.error("Error publishing ledger update notification for account {}", tradingAccountId, e);
        }
    }
}
