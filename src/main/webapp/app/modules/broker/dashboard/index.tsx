import React from 'react';
import { useEffect, useState } from 'react';
import { Alert, Spinner } from 'reactstrap';
import { getBrokerOverview, type BrokerOverview } from '../services/overview.service';
import UtilizationTable, { type UtilizationRow } from './components/UtilizationTable';
import RoleHelpPanel from 'app/shared/components/RoleHelpPanel';
import SimulatedBanner from 'app/shared/components/SimulatedBanner';

const BrokerDashboard: React.FC<{ brokerId?: number }> = ({ brokerId }) => {
  const [overview, setOverview] = useState<BrokerOverview | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const resolvedBrokerId = brokerId ?? 1; // placeholder until broker context is wired

  useEffect(() => {
    setLoading(true);
    setError(null);
    getBrokerOverview(resolvedBrokerId)
      .then(data => setOverview(data))
      .catch(e => setError(e?.message || 'Failed to load overview'))
      .finally(() => setLoading(false));
  }, [resolvedBrokerId]);

  const utilizationRows: UtilizationRow[] = []; // populated in later phases

  return (
    <div>
      <SimulatedBanner />
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h2>Broker Dashboard</h2>
        <RoleHelpPanel />
      </div>
      {loading && <div>Loading...</div>}
      {error && <div className="alert alert-danger">{error}</div>}
      {!loading && !error && !overview && (
        <Alert color="info">No overview data available. Overview will appear once traders are configured.</Alert>
      )}
      {overview && (
        <div className="row">
          <div className="col-md-4">
            <div className="card mb-3">
              <div className="card-body">
                <h5 className="card-title">Active Traders</h5>
                <p className="card-text display-6">{overview.activeTraderCount}</p>
              </div>
            </div>
          </div>
          <div className="col-md-4">
            <div className="card mb-3">
              <div className="card-body">
                <h5 className="card-title">Total Cash</h5>
                <p className="card-text display-6">{overview.totalCash}</p>
              </div>
            </div>
          </div>
          <div className="col-md-4">
            <div className="card mb-3">
              <div className="card-body">
                <h5 className="card-title">Total Exposure</h5>
                <p className="card-text display-6">{overview.totalEquityExposure}</p>
              </div>
            </div>
          </div>
        </div>
      )}
      {overview && (
        <>
          <h3>Top Utilization</h3>
          <UtilizationTable rows={utilizationRows} />
          {utilizationRows.length === 0 && (
            <Alert color="info" className="mt-3">
              No utilization data available.
            </Alert>
          )}
        </>
      )}
    </div>
  );
};

export default BrokerDashboard;
