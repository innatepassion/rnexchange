import React, { useEffect, useState } from 'react';
import { Table, Button, Alert, Spinner, UncontrolledTooltip, Modal, ModalHeader, ModalBody, ModalFooter } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faInfoCircle } from '@fortawesome/free-solid-svg-icons';
import {
  getBrokerSettlements,
  getClientStatements,
  type BrokerSettlementSummary,
  type StatementSummary,
} from './services/broker-settlements.service';
import SimulatedBanner from 'app/shared/components/SimulatedBanner';

const BrokerSettlementsModule: React.FC = () => {
  const [settlements, setSettlements] = useState<BrokerSettlementSummary[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [clientStatementsModal, setClientStatementsModal] = useState(false);
  const [clientStatements, setClientStatements] = useState<StatementSummary[]>([]);
  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const [loadingStatements, setLoadingStatements] = useState(false);

  useEffect(() => {
    loadSettlements();
  }, []);

  const loadSettlements = () => {
    setLoading(true);
    setError(null);
    getBrokerSettlements()
      .then(data => setSettlements(data))
      .catch(e => {
        setError(e?.message || 'Failed to load settlements');
        console.error('Error loading settlements:', e);
      })
      .finally(() => setLoading(false));
  };

  const handleViewSummary = (summaryUrl: string) => {
    window.open(summaryUrl, '_blank');
  };

  const handleViewClientStatements = async (refDate: string) => {
    setSelectedDate(refDate);
    setClientStatementsModal(true);
    setLoadingStatements(true);
    setClientStatements([]);
    try {
      const statements = await getClientStatements(refDate);
      setClientStatements(statements);
    } catch (e) {
      setError(e?.message || 'Failed to load client statements');
      console.error('Error loading client statements:', e);
    } finally {
      setLoadingStatements(false);
    }
  };

  const toggleClientStatementsModal = () => {
    setClientStatementsModal(!clientStatementsModal);
    if (!clientStatementsModal) {
      setClientStatements([]);
      setSelectedDate(null);
    }
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
    <div className="broker-settlements">
      <SimulatedBanner />
      <h2>
        Settlement Reports{' '}
        <FontAwesomeIcon icon={faInfoCircle} id="broker-settlement-info-tooltip" className="text-info ms-2" style={{ cursor: 'help' }} />
        <UncontrolledTooltip placement="right" target="broker-settlement-info-tooltip">
          <div style={{ textAlign: 'left', maxWidth: '300px' }}>
            <strong>Broker Settlement Summaries</strong>
            <br />
            <br />
            These summaries aggregate settlement data for all clients under your broker. They show:
            <ul style={{ marginBottom: 0, paddingLeft: '20px' }}>
              <li>Total client accounts processed</li>
              <li>Aggregate opening and closing balances</li>
              <li>Total EOD MTM P&L across all clients</li>
            </ul>
            <br />
            <strong>Training Context:</strong> All data is from simulated EOD settlement using internal mock prices for training purposes
            only.
          </div>
        </UncontrolledTooltip>
      </h2>
      <Alert color="info" className="mt-3">
        <strong>Simulated Environment:</strong> These settlement summaries are generated from simulated EOD data. Prices and P&L are from
        internal mock feeds for training purposes only.
      </Alert>
      {loading && (
        <div className="text-center py-4">
          <Spinner color="primary" />
          <span className="ms-2">Loading settlements...</span>
        </div>
      )}
      {error && (
        <Alert color="danger" className="mt-3">
          {error}
        </Alert>
      )}
      {!loading && !error && settlements.length === 0 && (
        <Alert color="info" className="mt-3">
          No settlement summaries available. Summaries will appear after EOD settlement runs.
        </Alert>
      )}
      {!loading && !error && settlements.length > 0 && (
        <Table striped responsive className="mt-3">
          <thead>
            <tr>
              <th>Date</th>
              <th>Broker</th>
              <th>Client Count</th>
              <th>Total Opening Balance</th>
              <th>Total Closing Balance</th>
              <th>Total EOD MTM P&L</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {settlements.map((settlement, index) => (
              <tr key={`${settlement.brokerId}-${settlement.refDate}-${index}`}>
                <td>{formatDate(settlement.refDate)}</td>
                <td>{settlement.brokerName}</td>
                <td>{settlement.totalClientCount}</td>
                <td>{formatCurrency(settlement.totalOpeningBalance)}</td>
                <td>
                  <strong>{formatCurrency(settlement.totalClosingBalance)}</strong>
                  {/* M6 User Story 3, Task T038: Highlight reconciliation status */}
                  {settlement.totalOpeningBalance + settlement.totalEodMtmPnl === settlement.totalClosingBalance && (
                    <span className="badge bg-success ms-2" title="Balances reconcile correctly">
                      ✓ Reconciled
                    </span>
                  )}
                </td>
                <td className={settlement.totalEodMtmPnl >= 0 ? 'text-success' : 'text-danger'}>
                  {formatCurrency(settlement.totalEodMtmPnl)}
                </td>
                <td>
                  <Button color="primary" size="sm" onClick={() => handleViewSummary(settlement.summaryUrl)} className="me-2">
                    View Summary
                  </Button>
                  <Button color="secondary" size="sm" onClick={() => handleViewClientStatements(settlement.refDate)}>
                    Client Statements
                  </Button>
                </td>
              </tr>
            ))}
          </tbody>
        </Table>
      )}

      <Modal isOpen={clientStatementsModal} toggle={toggleClientStatementsModal} size="lg">
        <ModalHeader toggle={toggleClientStatementsModal}>Client Statements - {selectedDate ? formatDate(selectedDate) : ''}</ModalHeader>
        <ModalBody>
          {loadingStatements && (
            <div className="text-center py-4">
              <Spinner color="primary" />
              <span className="ms-2">Loading client statements...</span>
            </div>
          )}
          {!loadingStatements && clientStatements.length === 0 && <Alert color="info">No client statements available for this date.</Alert>}
          {!loadingStatements && clientStatements.length > 0 && (
            <Table striped responsive>
              <thead>
                <tr>
                  <th>Account</th>
                  <th>Opening Balance</th>
                  <th>Net Cash Flows</th>
                  <th>EOD MTM P&L</th>
                  <th>Closing Balance</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {clientStatements.map(statement => (
                  <tr key={statement.id}>
                    <td>{statement.tradingAccountLabel || `Account ${statement.tradingAccountId}`}</td>
                    <td>{formatCurrency(statement.openingBalance)}</td>
                    <td>{formatCurrency(statement.netCashFlows || 0)}</td>
                    <td className={statement.eodMtmPnl >= 0 ? 'text-success' : 'text-danger'}>{formatCurrency(statement.eodMtmPnl)}</td>
                    <td>
                      <strong>{formatCurrency(statement.closingBalance)}</strong>
                    </td>
                    <td>
                      <Button color="primary" size="sm" onClick={() => window.open(statement.htmlUrl, '_blank')}>
                        View
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          )}
        </ModalBody>
        <ModalFooter>
          <Button color="secondary" onClick={toggleClientStatementsModal}>
            Close
          </Button>
        </ModalFooter>
      </Modal>
    </div>
  );
};

export default BrokerSettlementsModule;
