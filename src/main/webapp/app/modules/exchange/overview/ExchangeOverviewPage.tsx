/**
 * M6 User Story 3, Task T037: Exchange Overview / EOD Console
 *
 * Displays EOD batches and key metrics for exchange operators.
 */

import React, { useEffect, useState } from 'react';
import { Alert, Button, Card, CardBody, Col, Row, Spinner } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faChartLine, faSync, faInfoCircle } from '@fortawesome/free-solid-svg-icons';
import { Link } from 'react-router-dom';
import { getSettlementBatches, type SettlementBatchDTO } from '../settlement/services/settlement.service';

const ExchangeOverviewPage: React.FC = () => {
  const [recentBatches, setRecentBatches] = useState<SettlementBatchDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadRecentBatches();
  }, []);

  const loadRecentBatches = () => {
    setLoading(true);
    setError(null);
    const to = new Date();
    const from = new Date();
    from.setDate(from.getDate() - 7); // Last 7 days

    getSettlementBatches(from.toISOString().split('T')[0], to.toISOString().split('T')[0])
      .then(data => {
        setRecentBatches(data.slice(0, 5)); // Show last 5
        setLoading(false);
      })
      .catch(e => {
        setError(e?.message || 'Failed to load settlement batches');
        setLoading(false);
      });
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-IN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 2,
    }).format(value);
  };

  // Calculate summary metrics
  const totalAccountsProcessed = recentBatches.reduce((sum, b) => sum + (b.accountsProcessed || 0), 0);
  const totalPositionsProcessed = recentBatches.reduce((sum, b) => sum + (b.positionsProcessed || 0), 0);
  const totalNetPnl = recentBatches.reduce((sum, b) => sum + (b.netPnl || 0), 0);
  const processedBatches = recentBatches.filter(b => b.status === 'PROCESSED').length;

  return (
    <div className="exchange-overview">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h1>
          <FontAwesomeIcon icon={faChartLine} className="me-2" />
          Exchange Overview
        </h1>
        <Button color="primary" onClick={loadRecentBatches} disabled={loading}>
          <FontAwesomeIcon icon={faSync} className="me-2" />
          Refresh
        </Button>
      </div>

      <Alert color="info" className="mb-4">
        <FontAwesomeIcon icon={faInfoCircle} className="me-2" />
        <strong>Exchange Operator Console:</strong> Monitor settlement batches, system health, and exchange operations. All data is from
        simulated EOD settlement for training purposes.
      </Alert>

      {/* Summary Cards */}
      <Row className="mb-4">
        <Col md="3">
          <Card>
            <CardBody>
              <h5 className="card-title text-muted">Recent Batches</h5>
              <h2 className="mb-0">{recentBatches.length}</h2>
              <small className="text-muted">Last 7 days</small>
            </CardBody>
          </Card>
        </Col>
        <Col md="3">
          <Card>
            <CardBody>
              <h5 className="card-title text-muted">Accounts Processed</h5>
              <h2 className="mb-0">{totalAccountsProcessed}</h2>
              <small className="text-muted">Across all batches</small>
            </CardBody>
          </Card>
        </Col>
        <Col md="3">
          <Card>
            <CardBody>
              <h5 className="card-title text-muted">Positions Processed</h5>
              <h2 className="mb-0">{totalPositionsProcessed}</h2>
              <small className="text-muted">Total positions</small>
            </CardBody>
          </Card>
        </Col>
        <Col md="3">
          <Card>
            <CardBody>
              <h5 className="card-title text-muted">Total Net P&L</h5>
              <h2 className={`mb-0 ${totalNetPnl >= 0 ? 'text-success' : 'text-danger'}`}>{formatCurrency(totalNetPnl)}</h2>
              <small className="text-muted">Aggregate P&L</small>
            </CardBody>
          </Card>
        </Col>
      </Row>

      {/* Recent Batches */}
      <Card className="mb-4">
        <CardBody>
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h4>Recent Settlement Batches</h4>
            <Link to="/exchange/settlement">
              <Button color="link" size="sm">
                View All <FontAwesomeIcon icon={faChartLine} className="ms-1" />
              </Button>
            </Link>
          </div>

          {loading && (
            <div className="text-center py-4">
              <Spinner color="primary" />
              <span className="ms-2">Loading batches...</span>
            </div>
          )}

          {error && <Alert color="danger">{error}</Alert>}

          {!loading && !error && recentBatches.length === 0 && (
            <Alert color="info" className="mt-3">
              <strong>No settlement batches found</strong>
              <br />
              <small>Run EOD settlement to generate batches. Navigate to Settlement Management to run EOD for today.</small>
            </Alert>
          )}

          {!loading && !error && recentBatches.length > 0 && (
            <div className="table-responsive">
              <table className="table table-striped">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Status</th>
                    <th>Accounts</th>
                    <th>Positions</th>
                    <th>Net P&L</th>
                  </tr>
                </thead>
                <tbody>
                  {recentBatches.map(batch => (
                    <tr key={batch.id}>
                      <td>{formatDate(batch.refDate)}</td>
                      <td>
                        <span
                          className={`badge ${batch.status === 'PROCESSED' ? 'bg-success' : batch.status === 'FAILED' ? 'bg-danger' : 'bg-warning'}`}
                        >
                          {batch.status}
                        </span>
                      </td>
                      <td>{batch.accountsProcessed || 0}</td>
                      <td>{batch.positionsProcessed || 0}</td>
                      <td className={batch.netPnl && batch.netPnl >= 0 ? 'text-success' : 'text-danger'}>
                        {batch.netPnl ? formatCurrency(batch.netPnl) : '—'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardBody>
      </Card>

      {/* Quick Actions */}
      <Card>
        <CardBody>
          <h4 className="mb-3">Quick Actions</h4>
          <Row>
            <Col md="6">
              <Link to="/exchange/settlement">
                <Button color="primary" block className="mb-2">
                  <FontAwesomeIcon icon={faSync} className="me-2" />
                  Settlement Management
                </Button>
              </Link>
            </Col>
            <Col md="6">
              <Link to="/exchange-console">
                <Button color="secondary" block className="mb-2">
                  <FontAwesomeIcon icon={faChartLine} className="me-2" />
                  Market Data Console
                </Button>
              </Link>
            </Col>
          </Row>
        </CardBody>
      </Card>
    </div>
  );
};

export default ExchangeOverviewPage;
