import React, { useEffect, useState } from 'react';
import { Table, Button, Alert, Spinner } from 'reactstrap';
import { getStatements, type StatementSummary } from './services/statements.service';

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
      <h2>Daily Statements</h2>
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
