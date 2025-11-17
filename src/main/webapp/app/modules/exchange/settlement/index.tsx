import React from 'react';
import { Alert, UncontrolledTooltip } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faInfoCircle } from '@fortawesome/free-solid-svg-icons';

const ExchangeSettlementModule: React.FC = () => {
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
      <div className="mt-3">
        <p>Exchange Settlement module - Implementation in progress</p>
        <p className="text-muted">This module will allow Exchange Operators to run EOD settlement batches and view settlement history.</p>
      </div>
    </div>
  );
};

export default ExchangeSettlementModule;
