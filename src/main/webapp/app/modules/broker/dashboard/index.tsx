import React from 'react';
import { useEffect, useState } from 'react';
import { getBrokerOverview, type BrokerOverview } from '../services/overview.service';
import UtilizationTable, { type UtilizationRow } from './components/UtilizationTable';

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
      <h2>Broker Dashboard</h2>
      {loading && <div>Loading...</div>}
      {error && <div className="alert alert-danger">{error}</div>}
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
      <h3>Top Utilization</h3>
      <UtilizationTable rows={utilizationRows} />
    </div>
  );
};

export default BrokerDashboard;
