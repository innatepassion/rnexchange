import React from 'react';
import { useEffect, useState } from 'react';
import { Alert, Spinner, Button, Card, CardBody, Row, Col } from 'reactstrap';
import { Link } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faBook, faList, faUsers } from '@fortawesome/free-solid-svg-icons';
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

      <Row className="mt-4">
        <Col md="12">
          <h3>Quick Actions</h3>
        </Col>
      </Row>
      <Row>
        <Col md="4">
          <Card className="mb-3">
            <CardBody>
              <h5 className="card-title">
                <FontAwesomeIcon icon={faBook} className="me-2" />
                Funds Journal
              </h5>
              <p className="card-text">Create credit or debit journal entries for trader accounts.</p>
              <Link to="/broker/journal">
                <Button color="primary" block>
                  Create Journal Entry
                </Button>
              </Link>
            </CardBody>
          </Card>
        </Col>
        <Col md="4">
          <Card className="mb-3">
            <CardBody>
              <h5 className="card-title">
                <FontAwesomeIcon icon={faList} className="me-2" />
                View Journal Entries
              </h5>
              <p className="card-text">Search and view all journal entries with filters and pagination.</p>
              <Link to="/broker/journal-entries">
                <Button color="info" block>
                  View Entries
                </Button>
              </Link>
            </CardBody>
          </Card>
        </Col>
        <Col md="4">
          <Card className="mb-3">
            <CardBody>
              <h5 className="card-title">
                <FontAwesomeIcon icon={faUsers} className="me-2" />
                Manage Clients
              </h5>
              <p className="card-text">View and manage trader accounts under your broker.</p>
              <Link to="/broker/clients">
                <Button color="secondary" block>
                  View Clients
                </Button>
              </Link>
            </CardBody>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default BrokerDashboard;
