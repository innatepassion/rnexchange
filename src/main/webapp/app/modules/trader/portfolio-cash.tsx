import React, { useEffect, useState, useCallback } from 'react';
import { Spinner, Alert, Table, Badge, Row, Col, Card, CardBody, CardHeader } from 'reactstrap';
import { AxiosError } from 'axios';
import dayjs from 'dayjs';
import { getPositions, getLedgerEntries, getCashBalance, PositionView, LedgerEntryView, CashBalanceView } from 'app/shared/api/trading.api';

interface PortfolioCashProps {
  tradingAccountId: number;
}

const PortfolioCash: React.FC<PortfolioCashProps> = ({ tradingAccountId }) => {
  const [positions, setPositions] = useState<PositionView[]>([]);
  const [ledgerEntries, setLedgerEntries] = useState<LedgerEntryView[]>([]);
  const [cashBalance, setCashBalance] = useState<CashBalanceView | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      // Fetch positions, ledger entries, and cash balance in parallel
      const [positionsRes, ledgerRes, balanceRes] = await Promise.all([
        getPositions(tradingAccountId, 0, 100),
        getLedgerEntries(tradingAccountId, 0, 50),
        getCashBalance(tradingAccountId),
      ]);

      setPositions(positionsRes.data || []);
      setLedgerEntries(ledgerRes.data || []);
      setCashBalance(balanceRes.data || null);
    } catch (err) {
      let message = 'Failed to load portfolio and cash data';
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

  const formatPrice = (price?: number) => {
    if (price === undefined || price === null) return '—';
    return new Intl.NumberFormat('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(price);
  };

  const formatCurrency = (amount?: number) => {
    if (amount === undefined || amount === null) return '—';
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', minimumFractionDigits: 2 }).format(amount);
  };

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return '—';
    return dayjs(dateStr).format('DD/MM/YY HH:mm:ss');
  };

  const getTrendClass = (value?: number) => {
    if (!value) return '';
    return value > 0 ? 'text-success' : value < 0 ? 'text-danger' : '';
  };

  const getTrendIndicator = (value?: number) => {
    if (!value) return '—';
    return value > 0 ? '↑' : value < 0 ? '↓' : '→';
  };

  const formatPnL = (value?: number) => {
    if (value === undefined || value === null) return '—';
    const formatted = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', minimumFractionDigits: 2 }).format(
      Math.abs(value),
    );
    return value > 0 ? `+${formatted}` : value < 0 ? `-${formatted}` : formatted;
  };

  if (isLoading) {
    return (
      <div className="text-center py-4">
        <Spinner size="sm" className="me-2" /> Loading portfolio and cash data...
      </div>
    );
  }

  if (error) {
    return <Alert color="danger">{error}</Alert>;
  }

  const totalPortfolioValue = positions.reduce((sum, pos) => sum + (pos.mtm || 0), 0);

  return (
    <div className="portfolio-cash">
      {/* Cash Balance Summary */}
      <Row className="mb-4">
        <Col md="6">
          <Card>
            <CardHeader>
              <h5 className="mb-0">Available Cash</h5>
            </CardHeader>
            <CardBody>
              <div className="display-4 mb-2">{formatCurrency(cashBalance?.balance)}</div>
              <small className="text-muted">Last updated: {formatDate(cashBalance?.updatedAt)}</small>
            </CardBody>
          </Card>
        </Col>
        <Col md="6">
          <Card>
            <CardHeader>
              <h5 className="mb-0">Portfolio Value (MTM)</h5>
            </CardHeader>
            <CardBody>
              <div className={`display-4 mb-2 ${getTrendClass(totalPortfolioValue)}`}>{formatCurrency(totalPortfolioValue)}</div>
              <small className="text-muted">
                {positions.length} open position{positions.length !== 1 ? 's' : ''}
              </small>
            </CardBody>
          </Card>
        </Col>
      </Row>

      {/* Positions Table */}
      <Card className="mb-4">
        <CardHeader>
          <h5 className="mb-0">📈 Open Positions</h5>
        </CardHeader>
        <CardBody>
          {positions.length === 0 ? (
            <Alert color="info" className="mb-0">
              No open positions for this account. Place a BUY order to open your first position.
            </Alert>
          ) : (
            <Table responsive striped hover>
              <thead>
                <tr>
                  <th title="Stock symbol/code (e.g., RELIANCE)">Symbol</th>
                  <th title="Number of shares you own">Qty</th>
                  <th title="Weighted average cost per share: (units bought × prices) / total units">Avg Cost</th>
                  <th title="Current market price (mark-to-market)">Last Price</th>
                  <th title="Unrealized profit/loss if you sold at current price: (Last Price - Avg Cost) × Qty">MTM P&L (Unrealized)</th>
                  <th title="Unrealized profit/loss as a percentage">MTM %</th>
                  <th title="Profit/loss locked in from SELL orders">Realized P&L</th>
                </tr>
              </thead>
              <tbody>
                {positions.map(pos => {
                  const mtmPercent = pos.avgCost && pos.avgCost !== 0 ? (((pos.lastPx || 0) - pos.avgCost) / pos.avgCost) * 100 : 0;
                  return (
                    <tr key={pos.id} data-testid={`position-row-${pos.instrument?.symbol}`}>
                      <td>
                        <strong>{pos.instrument?.symbol || 'UNKNOWN'}</strong>
                        <br />
                        <small className="text-muted">{pos.instrument?.exchange || ''}</small>
                      </td>
                      <td>{pos.qty || 0}</td>
                      <td>{formatPrice(pos.avgCost)}</td>
                      <td>{formatPrice(pos.lastPx)}</td>
                      <td>
                        <span className={getTrendClass(pos.unrealizedPnl)}>
                          {getTrendIndicator(pos.unrealizedPnl)} {formatCurrency(pos.unrealizedPnl)}
                        </span>
                      </td>
                      <td>
                        <span className={getTrendClass(mtmPercent)}>
                          {getTrendIndicator(mtmPercent)} {mtmPercent.toFixed(2)}%
                        </span>
                      </td>
                      <td>
                        <span className={getTrendClass(pos.realizedPnl)}>{pos.realizedPnl ? formatPnL(pos.realizedPnl) : '—'}</span>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </Table>
          )}
        </CardBody>
      </Card>

      {/* Recent Ledger Entries */}
      <Card className="mb-4">
        <CardHeader>
          <h5 className="mb-0">💰 Recent Transactions (Ledger)</h5>
        </CardHeader>
        <CardBody>
          {ledgerEntries.length === 0 ? (
            <Alert color="info" className="mb-0">
              No recent transactions for this account. Each BUY or SELL order will create a transaction here.
            </Alert>
          ) : (
            <Table responsive striped hover size="sm">
              <thead>
                <tr>
                  <th title="When the transaction occurred">Date/Time</th>
                  <th title="DEBIT: cash out (buying) | CREDIT: cash in (selling)">Type</th>
                  <th title="Amount debited or credited">Amount</th>
                  <th title="Trading fee charged per transaction (₹25)">Fee</th>
                  <th title="Detailed transaction description including symbol, quantity, price, and P&L">Description</th>
                </tr>
              </thead>
              <tbody>
                {ledgerEntries.slice(0, 15).map(entry => {
                  const isSell = entry.description && entry.description.toUpperCase().includes('SELL');
                  return (
                    <tr key={entry.id} data-testid={`ledger-row-${entry.id}`} className={isSell ? 'table-warning' : ''}>
                      <td>{formatDate(entry.createdAt)}</td>
                      <td>
                        <Badge color={entry.type === 'DEBIT' ? 'danger' : entry.type === 'CREDIT' ? 'success' : 'secondary'}>
                          {entry.type}
                        </Badge>
                      </td>
                      <td>
                        <span className={entry.type === 'DEBIT' ? 'text-danger' : entry.type === 'CREDIT' ? 'text-success' : ''}>
                          {entry.type === 'DEBIT' ? '-' : entry.type === 'CREDIT' ? '+' : ''}
                          {formatCurrency(entry.amount)}
                        </span>
                      </td>
                      <td>{entry.fee ? formatCurrency(entry.fee) : '—'}</td>
                      <td className="small">
                        {entry.description || '—'}
                        {isSell && <span className="ms-2 badge bg-info">SELL</span>}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </Table>
          )}
        </CardBody>
      </Card>

      <div className="small text-muted mt-4 p-3 bg-light rounded">
        <p className="mb-2">
          <strong>⚠️ Simulated Environment:</strong> This is a training-only environment. Prices and portfolio values are based on mock data
          and do not represent real market conditions.
        </p>
        <p className="mb-2">
          <strong>📊 Understanding Your Portfolio:</strong>
        </p>
        <ul className="mb-2">
          <li>
            <strong>Unrealized P&L (MTM):</strong> Your profit/loss on open positions, calculated at the current market price.
          </li>
          <li>
            <strong>Realized P&L:</strong> Your actual profit/loss when you sell a position (SELL transactions credit your account with
            profits).
          </li>
          <li>
            <strong>Average Cost:</strong> The weighted average price of all shares in a position, used to calculate your profit/loss.
          </li>
        </ul>
        <p className="mb-0">
          <strong>💡 Tip:</strong> When you SELL, look for the SELL badge in the Recent Transactions table to see the credit to your
          account, and check the Realized P&L column to see your profit or loss.
        </p>
      </div>
    </div>
  );
};

export default PortfolioCash;
