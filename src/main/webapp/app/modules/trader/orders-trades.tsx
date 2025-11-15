import React, { useEffect, useState, useCallback } from 'react';
import { Spinner, Alert, Table, Badge, Card, CardBody, CardHeader } from 'reactstrap';
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
    return (
      <div className="orders-trades">
        <Alert color="info">No orders or trades yet for this account.</Alert>
        <Card>
          <CardBody>
            <p className="mb-2">
              <strong>💡 Getting Started:</strong> Once you place your first order, it will appear here along with execution details.
            </p>
            <p className="mb-0">Use the Order Ticket drawer in Market Watch to place a BUY or SELL order.</p>
          </CardBody>
        </Card>
      </div>
    );
  }

  return (
    <div className="orders-trades">
      <Card className="mb-3">
        <CardHeader>
          <h5 className="mb-0">📋 Orders & Trades</h5>
        </CardHeader>
        <CardBody>
          <p className="mb-2">
            <strong>⚠️ Simulated Environment:</strong> This is a mock trading platform for learning. All prices and fills are simulated.
          </p>
          <p className="mb-0">
            <strong>📊 Understanding the Table:</strong> Each row represents an order you placed (Order) or a fill you received (Execution).
            Orders show your request; Executions show when and at what price your order was filled. SELL transactions show when you closed a
            position and locked in profits/losses.
          </p>
        </CardBody>
      </Card>
      <Table responsive striped hover>
        <thead>
          <tr>
            <th title="The date and time the order was placed or executed">Date/Time</th>
            <th title="Stock symbol (e.g., RELIANCE, INFY)">Symbol</th>
            <th title="BUY to increase holdings, SELL to decrease holdings">Side</th>
            <th title="MARKET: execute immediately | LIMIT: execute only at your price">Type</th>
            <th title="Number of units">Qty</th>
            <th title="Price per unit (empty for orders, filled for executions)">Price</th>
            <th title="Order status: FILLED = completed, REJECTED = failed, etc.">Status</th>
            <th title="Additional execution and P&L details">Details</th>
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
                      {isSell && (
                        <span className="ms-2 badge bg-warning text-dark" title="This SELL locked in profits or losses from your position">
                          REALIZED P&L
                        </span>
                      )}
                    </>
                  )}
                  {!isExecution && trade.orderId && <span>Order #{trade.orderId}</span>}
                </td>
              </tr>
            );
          })}
        </tbody>
      </Table>
      <div className="small text-muted mt-3 p-3 bg-light rounded">
        <p className="mb-2">
          <strong>🎓 Learning Tips:</strong>
        </p>
        <ul className="mb-0">
          <li>
            <strong>MARKET orders</strong> fill immediately at the best available price. Use for quick entry/exit.
          </li>
          <li>
            <strong>LIMIT orders</strong> only fill if the price reaches your specified level. Protective but may not fill.
          </li>
          <li>
            <strong>REALIZED P&L</strong> appears when you SELL: profit/loss = (sell price - avg cost) × qty. Check ledger for details.
          </li>
          <li>
            <strong>Status = REJECTED?</strong> Check the error message (insufficient funds, inactive instrument, etc.) and try again.
          </li>
        </ul>
      </div>
    </div>
  );
};

export default OrdersTrades;
