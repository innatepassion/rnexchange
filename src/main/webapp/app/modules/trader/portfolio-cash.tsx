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
          <h5 className="mb-0">Open Positions</h5>
        </CardHeader>
        <CardBody>
          {positions.length === 0 ? (
            <Alert color="info" className="mb-0">
              No open positions for this account.
            </Alert>
          ) : (
            <Table responsive striped hover>
              <thead>
                <tr>
                  <th>Symbol</th>
                  <th>Qty</th>
                  <th>Avg Cost</th>
                  <th>Last Price</th>
                  <th>MTM P&L</th>
                  <th>MTM %</th>
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
                        <span className={getTrendClass(pos.mtm)}>
                          {getTrendIndicator(pos.mtm)} {formatCurrency(pos.mtm)}
                        </span>
                      </td>
                      <td>
                        <span className={getTrendClass(mtmPercent)}>
                          {getTrendIndicator(mtmPercent)} {mtmPercent.toFixed(2)}%
                        </span>
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
      <Card>
        <CardHeader>
          <h5 className="mb-0">Recent Transactions</h5>
        </CardHeader>
        <CardBody>
          {ledgerEntries.length === 0 ? (
            <Alert color="info" className="mb-0">
              No recent transactions for this account.
            </Alert>
          ) : (
            <Table responsive striped hover size="sm">
              <thead>
                <tr>
                  <th>Date/Time</th>
                  <th>Type</th>
                  <th>Amount</th>
                  <th>Fee</th>
                  <th>Description</th>
                </tr>
              </thead>
              <tbody>
                {ledgerEntries.slice(0, 15).map(entry => (
                  <tr key={entry.id} data-testid={`ledger-row-${entry.id}`}>
                    <td>{formatDate(entry.createdAt)}</td>
                    <td>
                      <Badge color={entry.type === 'DEBIT' ? 'danger' : 'success'}>{entry.type}</Badge>
                    </td>
                    <td>
                      <span className={entry.type === 'DEBIT' ? 'text-danger' : 'text-success'}>
                        {entry.type === 'DEBIT' ? '-' : '+'}
                        {formatCurrency(entry.amount)}
                      </span>
                    </td>
                    <td>{entry.fee ? formatCurrency(entry.fee) : '—'}</td>
                    <td className="small">{entry.description || '—'}</td>
                  </tr>
                ))}
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
        <p className="mb-0">
          <strong>Mark-to-Market (MTM):</strong> Unrealized profit/loss calculated at the last quoted price. Realized P&L is settled when
          positions are closed.
        </p>
      </div>
    </div>
  );
};

export default PortfolioCash;
