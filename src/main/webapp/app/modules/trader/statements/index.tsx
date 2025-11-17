import React, { useEffect, useState } from 'react';
import { Table, Button, Alert, Spinner, UncontrolledTooltip } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faInfoCircle } from '@fortawesome/free-solid-svg-icons';
import { getStatements, type StatementSummary } from './services/statements.service';
import SimulatedBanner from 'app/shared/components/SimulatedBanner';

const TraderStatementsModule: React.FC = () => {
  const [statements, setStatements] = useState<StatementSummary[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadStatements();
  }, []);

  const loadStatements = () => {
    setLoading(true);
    setError(null);
    getStatements()
      .then(data => setStatements(data))
      .catch(e => {
        setError(e?.message || 'Failed to load statements');
        console.error('Error loading statements:', e);
      })
      .finally(() => setLoading(false));
  };

  const handleViewStatement = (htmlUrl: string) => {
    window.open(htmlUrl, '_blank');
  };

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 2,
    }).format(value);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-IN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  return (
    <div className="trader-statements">
      <SimulatedBanner />
      <h2>
        Daily Statements{' '}
        <FontAwesomeIcon icon={faInfoCircle} id="trader-statements-info-tooltip" className="text-info ms-2" style={{ cursor: 'help' }} />
        <UncontrolledTooltip placement="right" target="trader-statements-info-tooltip">
          <div style={{ textAlign: 'left', maxWidth: '300px' }}>
            <strong>Daily Trading Statements</strong>
            <br />
            <br />
            Your daily statements show:
            <ul style={{ marginBottom: 0, paddingLeft: '20px' }}>
              <li>Opening balance at start of day</li>
              <li>All cash flows (deposits, withdrawals)</li>
              <li>EOD MTM P&L from settlement</li>
              <li>Closing balance</li>
              <li>Complete ledger entries for the day</li>
            </ul>
            <br />
            <strong>Training Context:</strong> Statements are generated from simulated EOD settlement data. Prices and P&L use internal mock
            feeds for training purposes only.
          </div>
        </UncontrolledTooltip>
      </h2>
      <Alert color="info" className="mt-3">
        <strong>Simulated Environment:</strong> These statements are generated from simulated EOD settlement data. Prices and P&L are from
        internal mock feeds for training purposes only.
      </Alert>
      {loading && (
        <div className="text-center py-4">
          <Spinner color="primary" />
          <span className="ms-2">Loading statements...</span>
        </div>
      )}
      {error && (
        <Alert color="danger" className="mt-3">
          {error}
        </Alert>
      )}
      {!loading && !error && statements.length === 0 && (
        <Alert color="info" className="mt-3">
          No statements available. Statements will appear after EOD settlement runs.
        </Alert>
      )}
      {!loading && !error && statements.length > 0 && (
        <Table striped responsive className="mt-3">
          <thead>
            <tr>
              <th>Date</th>
              <th>Account</th>
              <th>Opening Balance</th>
              <th>Net Cash Flows</th>
              <th>EOD MTM P&L</th>
              <th>Closing Balance</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {statements.map(statement => (
              <tr key={statement.id}>
                <td>{formatDate(statement.refDate)}</td>
                <td>{statement.tradingAccountLabel || `Account ${statement.tradingAccountId}`}</td>
                <td>{formatCurrency(statement.openingBalance)}</td>
                <td>{formatCurrency(statement.netCashFlows || 0)}</td>
                <td className={statement.eodMtmPnl >= 0 ? 'text-success' : 'text-danger'}>{formatCurrency(statement.eodMtmPnl)}</td>
                <td>
                  <strong>{formatCurrency(statement.closingBalance)}</strong>
                  {/* M6 User Story 3, Task T038: Highlight reconciliation status */}
                  {Math.abs(statement.openingBalance + (statement.netCashFlows || 0) + statement.eodMtmPnl - statement.closingBalance) <
                    0.01 && (
                    <span className="badge bg-success ms-2" title="Balances reconcile correctly">
                      ✓ Reconciled
                    </span>
                  )}
                </td>
                <td>
                  <Button color="primary" size="sm" onClick={() => handleViewStatement(statement.htmlUrl)}>
                    View
                  </Button>
                </td>
              </tr>
            ))}
          </tbody>
        </Table>
      )}
    </div>
  );
};

export default TraderStatementsModule;
