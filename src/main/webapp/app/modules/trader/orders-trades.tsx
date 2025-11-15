import React, { useEffect, useState, useCallback } from 'react';
import { Spinner, Alert, Table, Badge } from 'reactstrap';
import { AxiosError } from 'axios';
import dayjs from 'dayjs';
import { getOrders, getExecutions } from 'app/shared/api/trading.api';
import type { IOrder } from 'app/shared/model/order.model';
import type { IExecution } from 'app/shared/model/execution.model';

interface OrdersTradesProps {
  tradingAccountId: number;
}

interface TradeRecord {
  id: string;
  orderId?: number;
  executionId?: number;
  symbol?: string;
  side: 'BUY' | 'SELL';
  type?: string;
  quantity: number;
  price?: number;
  status: string;
  createdAt?: string;
  executedAt?: string;
}

const OrdersTrades: React.FC<OrdersTradesProps> = ({ tradingAccountId }) => {
  const [trades, setTrades] = useState<TradeRecord[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      // Fetch orders and executions in parallel
      const [ordersRes, executionsRes] = await Promise.all([getOrders(tradingAccountId, 0, 50), getExecutions(tradingAccountId, 0, 50)]);

      const orders = ordersRes.data || [];
      const executions = executionsRes.data || [];

      // Create a map of orders for quick lookup
      const orderMap = new Map<number, IOrder>();
      orders.forEach(order => {
        if (order.id) {
          orderMap.set(order.id, order);
        }
      });

      // Combine orders and executions into a single list sorted by date
      const combined: TradeRecord[] = [];

      // Add orders
      orders.forEach(order => {
        combined.push({
          id: `order-${order.id}`,
          orderId: order.id,
          symbol: order.instrument?.symbol,
          side: (order.side as 'BUY' | 'SELL') || 'BUY',
          type: order.type,
          quantity: order.qty || 0,
          status: order.status || 'UNKNOWN',
          createdAt: order.createdAt?.toString(),
        });
      });

      // Add executions
      executions.forEach(execution => {
        combined.push({
          id: `exec-${execution.id}`,
          executionId: execution.id,
          orderId: execution.order?.id,
          symbol: execution.order?.instrument?.symbol,
          side: (execution.order?.side as 'BUY' | 'SELL') || 'BUY',
          quantity: execution.qty || 0,
          price: execution.px,
          status: 'EXECUTED',
          executedAt: execution.execTs?.toString(),
        });
      });

      // Sort by date (newest first)
      combined.sort((a, b) => {
        const dateA = new Date(a.createdAt || a.executedAt || 0).getTime();
        const dateB = new Date(b.createdAt || b.executedAt || 0).getTime();
        return dateB - dateA;
      });

      setTrades(combined.slice(0, 20)); // Show last 20 trades
    } catch (err) {
      let message = 'Failed to load orders and trades';
      if (err instanceof AxiosError) {
        message = (err.response?.data?.message as string) || err.message || message;
      } else if (err instanceof Error) {
        message = err.message;
      }
      setError(message);
    } finally {
      setIsLoading(false);
    }
  }, [tradingAccountId]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const getStatusBadgeColor = (status: string) => {
    switch (status.toUpperCase()) {
      case 'FILLED':
      case 'EXECUTED':
        return 'success';
      case 'PENDING':
      case 'NEW':
      case 'ACCEPTED':
        return 'info';
      case 'REJECTED':
        return 'danger';
      default:
        return 'secondary';
    }
  };

  const formatPrice = (price?: number) => {
    if (price === undefined || price === null) return '—';
    return new Intl.NumberFormat('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(price);
  };

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return '—';
    return dayjs(dateStr).format('DD/MM/YY HH:mm:ss');
  };

  const getSideColor = (side: 'BUY' | 'SELL') => {
    return side === 'BUY' ? 'success' : 'danger';
  };

  if (isLoading) {
    return (
      <div className="text-center py-4">
        <Spinner size="sm" className="me-2" /> Loading orders and trades...
      </div>
    );
  }

  if (error) {
    return <Alert color="danger">{error}</Alert>;
  }

  if (trades.length === 0) {
    return <Alert color="info">No orders or trades yet for this account.</Alert>;
  }

  return (
    <div className="orders-trades">
      <Table responsive striped hover>
        <thead>
          <tr>
            <th>Date/Time</th>
            <th>Symbol</th>
            <th>Side</th>
            <th>Type</th>
            <th>Qty</th>
            <th>Price</th>
            <th>Status</th>
            <th>Details</th>
          </tr>
        </thead>
        <tbody>
          {trades.map(trade => {
            const isSell = trade.side === 'SELL';
            const isExecution = trade.executionId !== undefined;
            return (
              <tr key={trade.id} data-testid={`trade-row-${trade.id}`} className={isSell ? 'table-light' : ''}>
                <td>{formatDate(trade.createdAt || trade.executedAt)}</td>
                <td>
                  <strong>{trade.symbol || '—'}</strong>
                </td>
                <td>
                  <Badge color={getSideColor(trade.side)} className="fw-bold">
                    {trade.side}
                  </Badge>
                </td>
                <td>{trade.type || '—'}</td>
                <td>{trade.quantity}</td>
                <td>{formatPrice(trade.price)}</td>
                <td>
                  <Badge color={getStatusBadgeColor(trade.status)}>{trade.status}</Badge>
                </td>
                <td className="small">
                  {isExecution && (
                    <>
                      <span>Exec #{trade.executionId}</span>
                      {isSell && <span className="ms-2 badge bg-warning text-dark">REALIZED P&L</span>}
                    </>
                  )}
                  {!isExecution && trade.orderId && <span>Order #{trade.orderId}</span>}
                </td>
              </tr>
            );
          })}
        </tbody>
      </Table>
    </div>
  );
};

export default OrdersTrades;
