import React, { useEffect, useState } from 'react';
import { Alert, Button, UncontrolledTooltip, Spinner } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faInfoCircle, faSync } from '@fortawesome/free-solid-svg-icons';
import { SettlementBatchTable } from './components/SettlementBatchTable';
import { runEod, getSettlementBatches, type SettlementBatchDTO } from './services/settlement.service';

const ExchangeSettlementModule: React.FC = () => {
  const [batches, setBatches] = useState<SettlementBatchDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [runningEod, setRunningEod] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [polling, setPolling] = useState(false);

  // Date range for batch listing (last 30 days)
  const getDateRange = () => {
    const to = new Date();
    const from = new Date();
    from.setDate(from.getDate() - 30);
    return {
      from: from.toISOString().split('T')[0],
      to: to.toISOString().split('T')[0],
    };
  };

  useEffect(() => {
    loadBatches();
  }, []);

  const loadBatches = () => {
    setLoading(true);
    setError(null);
    const { from, to } = getDateRange();
    getSettlementBatches(from, to)
      .then(data => {
        setBatches(data);
        setLoading(false);
      })
      .catch(e => {
        setError(e?.message || 'Failed to load settlement batches');
        console.error('Error loading batches:', e);
        setLoading(false);
      });
  };

  const handleRunEodForToday = () => {
    const today = new Date().toISOString().split('T')[0];
    handleRunEod(today);
  };

  const handleRunEod = (date: string) => {
    setRunningEod(true);
    setError(null);
    setSuccess(null);

    runEod(date)
      .then(batch => {
        setSuccess(`EOD settlement started for ${date}. Batch ID: ${batch.id}`);
        // Start polling for status updates
        setPolling(true);
        pollBatchStatus(batch.id, date);
        // Reload batches list
        setTimeout(() => {
          loadBatches();
        }, 1000);
      })
      .catch(e => {
        setError(e?.response?.data?.message || e?.message || 'Failed to run EOD settlement');
        console.error('Error running EOD:', e);
        setRunningEod(false);
      });
  };

  const pollBatchStatus = (batchId: number, date: string) => {
    const pollInterval = setInterval(() => {
      const { from, to } = getDateRange();
      getSettlementBatches(from, to)
        .then(data => {
          const batch = data.find(b => b.id === batchId);
          if (batch && (batch.status === 'PROCESSED' || batch.status === 'FAILED')) {
            clearInterval(pollInterval);
            setPolling(false);
            setRunningEod(false);
            if (batch.status === 'PROCESSED') {
              setSuccess(`EOD settlement completed successfully for ${date}`);
            } else {
              setError(`EOD settlement failed for ${date}`);
            }
            loadBatches();
          }
        })
        .catch(e => {
          console.error('Error polling batch status:', e);
        });
    }, 2000); // Poll every 2 seconds

    // Stop polling after 5 minutes
    setTimeout(() => {
      clearInterval(pollInterval);
      setPolling(false);
      setRunningEod(false);
    }, 300000);
  };

  const handleReRunForDate = (date: string) => {
    if (
      window.confirm(
        `Are you sure you want to re-run EOD settlement for ${date}? This will supersede any previous EOD entries for this date.`,
      )
    ) {
      handleRunEod(date);
    }
  };

  return (
    <div className="exchange-settlement">
      <h2>
        Settlement Management{' '}
        <FontAwesomeIcon icon={faInfoCircle} id="settlement-info-tooltip" className="text-info ms-2" style={{ cursor: 'help' }} />
        <UncontrolledTooltip placement="right" target="settlement-info-tooltip">
          <div style={{ textAlign: 'left', maxWidth: '300px' }}>
            <strong>Simulated EOD Settlement</strong>
            <br />
            <br />
            This is a training environment. EOD (End-of-Day) settlement uses simulated internal prices from mock market data.
            <br />
            <br />
            <strong>How it works:</strong>
            <ul style={{ marginBottom: 0, paddingLeft: '20px' }}>
              <li>Marks all open positions to internal settlement prices</li>
              <li>Calculates MTM (Mark-to-Market) P&L per account</li>
              <li>Posts EOD MTM entries to ledgers</li>
              <li>Updates position snapshots</li>
            </ul>
            <br />
            <strong>Note:</strong> Prices and P&L are for training purposes only and do not reflect real market data.
          </div>
        </UncontrolledTooltip>
      </h2>
      <Alert color="info" className="mt-3">
        <strong>Simulated Environment Notice:</strong> EOD settlement in this system uses internal mock prices for training purposes.
        Settlement batches, positions, and ledger entries are generated from simulated data and should not be used for real trading
        decisions.
      </Alert>

      {error && (
        <Alert color="danger" className="mt-3" toggle={() => setError(null)}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert color="success" className="mt-3" toggle={() => setSuccess(null)}>
          {success}
        </Alert>
      )}

      <div className="mt-3 d-flex justify-content-between align-items-center">
        <div>
          <Button color="primary" onClick={handleRunEodForToday} disabled={runningEod || polling} className="me-2">
            {runningEod || polling ? (
              <>
                <Spinner size="sm" className="me-2" />
                Running EOD...
              </>
            ) : (
              <>
                <FontAwesomeIcon icon={faSync} className="me-2" />
                Run EOD for Today
              </>
            )}
          </Button>
          <Button color="secondary" onClick={loadBatches} disabled={loading}>
            Refresh
          </Button>
        </div>
      </div>

      <div className="mt-4">
        <h4>Settlement Batches (Last 30 Days)</h4>
        <SettlementBatchTable batches={batches} loading={loading} />
      </div>

      {batches.length > 0 && (
        <div className="mt-3">
          <p className="text-muted small">
            <strong>Note:</strong> To re-run EOD for a specific date, click on the date row or use the API directly. Re-running will
            supersede previous EOD entries for that date while maintaining an audit trail.
          </p>
        </div>
      )}
    </div>
  );
};

export default ExchangeSettlementModule;
