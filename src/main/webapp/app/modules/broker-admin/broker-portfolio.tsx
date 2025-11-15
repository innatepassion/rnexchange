import React, { useEffect, useState } from 'react';
import {
  Alert,
  Badge,
  Button,
  Card,
  CardBody,
  CardHeader,
  Col,
  Container,
  Nav,
  NavItem,
  NavLink,
  Row,
  Spinner,
  Table,
  TabContent,
  TabPane,
} from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faBriefcase, faChartLine, faExchangeAlt, faTimesCircle, faCheckCircle } from '@fortawesome/free-solid-svg-icons';
import axios, { AxiosError } from 'axios';

/**
 * T026: Broker Admin Portfolio Dashboard
 *
 * Provides back-office views for Broker Admin users to:
 * - View all orders placed by traders under their broker
 * - View positions held by traders under their broker
 * - View cash ledger entries for traders under their broker
 *
 * Phase 5, User Story 3: Broker Admin views trading activity and balances
 */
interface OrderData {
  id: number;
  tradingAccountId: number;
  instrumentId: number;
  instrumentSymbol?: string;
  side: 'BUY' | 'SELL';
  type: 'MARKET' | 'LIMIT';
  qty: number;
  limitPx?: number;
  status: 'NEW' | 'ACCEPTED' | 'FILLED' | 'REJECTED';
  createdAt: string;
  executionPrice?: number;
}

interface PositionData {
  id: number;
  tradingAccountId: number;
  instrumentSymbol?: string;
  qty: number;
  avgCost: number;
  lastPrice?: number;
}

interface LedgerEntryData {
  id: number;
  tradingAccountId: number;
  amount: number;
  type: 'DEBIT' | 'CREDIT';
  description: string;
  createdAt: string;
}

export const BrokerPortfolio: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'orders' | 'positions' | 'ledger'>('orders');
  const [orders, setOrders] = useState<OrderData[]>([]);
  const [positions, setPositions] = useState<PositionData[]>([]);
  const [ledgerEntries, setLedgerEntries] = useState<LedgerEntryData[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lastUpdate, setLastUpdate] = useState<Date | null>(null);

  // Fetch broker admin data
  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [ordersRes, positionsRes, ledgerRes] = await Promise.all([
        axios.get<OrderData[]>('/api/admin/portfolio/orders'),
        axios.get<PositionData[]>('/api/admin/portfolio/positions'),
        axios.get<LedgerEntryData[]>('/api/admin/portfolio/ledger-entries'),
      ]);

      setOrders(ordersRes.data || []);
      setPositions(positionsRes.data || []);
      setLedgerEntries(ledgerRes.data || []);
      setLastUpdate(new Date());
    } catch (err) {
      const axiosError = err as AxiosError;
      setError(
        axiosError.response?.status === 403
          ? 'You do not have permission to view broker portfolio data.'
          : 'Failed to load broker portfolio data. Please try again.',
      );
      console.error('Error fetching broker portfolio data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleRefresh = () => {
    fetchData();
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'FILLED':
        return <Badge color="success">{status}</Badge>;
      case 'ACCEPTED':
        return <Badge color="info">{status}</Badge>;
      case 'REJECTED':
        return <Badge color="danger">{status}</Badge>;
      case 'NEW':
      default:
        return <Badge color="warning">{status}</Badge>;
    }
  };

  const getLedgerBadge = (type: string) => {
    return type === 'DEBIT' ? (
      <Badge color="danger">
        <FontAwesomeIcon icon={faTimesCircle} /> {type}
      </Badge>
    ) : (
      <Badge color="success">
        <FontAwesomeIcon icon={faCheckCircle} /> {type}
      </Badge>
    );
  };

  return (
    <Container fluid>
      <Row className="mb-4">
        <Col md="8">
          <h2>
            <FontAwesomeIcon icon={faBriefcase} className="me-2" />
            Broker Admin Portfolio
          </h2>
          <small className="text-muted">View orders, positions, and cash balances for all traders under your broker</small>
        </Col>
        <Col md="4" className="text-end">
          <Button color="primary" onClick={handleRefresh} disabled={loading}>
            {loading ? <Spinner size="sm" /> : '↻'} Refresh
          </Button>
        </Col>
      </Row>

      {error && (
        <Alert color="danger">
          <strong>Error:</strong> {error}
        </Alert>
      )}

      {lastUpdate && (
        <Row className="mb-3">
          <Col>
            <small className="text-muted">Last updated: {lastUpdate.toLocaleTimeString()}</small>
          </Col>
        </Row>
      )}

      <Card>
        <CardHeader>
          <Nav tabs>
            <NavItem>
              <NavLink active={activeTab === 'orders'} onClick={() => setActiveTab('orders')} style={{ cursor: 'pointer' }}>
                <FontAwesomeIcon icon={faExchangeAlt} className="me-2" />
                Orders ({orders.length})
              </NavLink>
            </NavItem>
            <NavItem>
              <NavLink active={activeTab === 'positions'} onClick={() => setActiveTab('positions')} style={{ cursor: 'pointer' }}>
                <FontAwesomeIcon icon={faChartLine} className="me-2" />
                Positions ({positions.length})
              </NavLink>
            </NavItem>
            <NavItem>
              <NavLink active={activeTab === 'ledger'} onClick={() => setActiveTab('ledger')} style={{ cursor: 'pointer' }}>
                Ledger ({ledgerEntries.length})
              </NavLink>
            </NavItem>
          </Nav>
        </CardHeader>
        <CardBody>
          <TabContent activeTab={activeTab}>
            {/* Orders Tab */}
            <TabPane tabId="orders">
              {loading ? (
                <div className="text-center py-4">
                  <Spinner color="primary" />
                </div>
              ) : orders.length === 0 ? (
                <Alert color="info">No orders found for traders under your broker.</Alert>
              ) : (
                <div className="table-responsive">
                  <Table hover>
                    <thead>
                      <tr>
                        <th>Order ID</th>
                        <th>Instrument</th>
                        <th>Side</th>
                        <th>Type</th>
                        <th>Qty</th>
                        <th>Limit Price</th>
                        <th>Execution Price</th>
                        <th>Status</th>
                        <th>Created</th>
                      </tr>
                    </thead>
                    <tbody>
                      {orders.map(order => (
                        <tr key={order.id}>
                          <td>#{order.id}</td>
                          <td>{order.instrumentSymbol || 'Unknown'}</td>
                          <td>
                            <Badge color={order.side === 'BUY' ? 'info' : 'danger'}>{order.side}</Badge>
                          </td>
                          <td>{order.type}</td>
                          <td>{order.qty}</td>
                          <td>{order.limitPx ? order.limitPx.toFixed(2) : '-'}</td>
                          <td>{order.executionPrice ? order.executionPrice.toFixed(2) : '-'}</td>
                          <td>{getStatusBadge(order.status)}</td>
                          <td>{new Date(order.createdAt).toLocaleString()}</td>
                        </tr>
                      ))}
                    </tbody>
                  </Table>
                </div>
              )}
            </TabPane>

            {/* Positions Tab */}
            <TabPane tabId="positions">
              {loading ? (
                <div className="text-center py-4">
                  <Spinner color="primary" />
                </div>
              ) : positions.length === 0 ? (
                <Alert color="info">No positions found for traders under your broker.</Alert>
              ) : (
                <div className="table-responsive">
                  <Table hover>
                    <thead>
                      <tr>
                        <th>Instrument</th>
                        <th>Qty</th>
                        <th>Avg Cost</th>
                        <th>Last Price</th>
                        <th>MTM Value</th>
                        <th>MTM %</th>
                      </tr>
                    </thead>
                    <tbody>
                      {positions.map(pos => {
                        const mtmValue = (pos.lastPrice || pos.avgCost - pos.avgCost) * pos.qty;
                        const mtmPercent = (((pos.lastPrice || pos.avgCost) - pos.avgCost) / pos.avgCost) * 100;
                        return (
                          <tr key={pos.id}>
                            <td>{pos.instrumentSymbol || 'Unknown'}</td>
                            <td>{pos.qty}</td>
                            <td>₹{pos.avgCost.toFixed(2)}</td>
                            <td>{pos.lastPrice ? `₹${pos.lastPrice.toFixed(2)}` : '-'}</td>
                            <td>
                              <Badge color={mtmValue >= 0 ? 'success' : 'danger'}>₹{Math.abs(mtmValue).toFixed(2)}</Badge>
                            </td>
                            <td>
                              <Badge color={mtmPercent >= 0 ? 'success' : 'danger'}>{mtmPercent.toFixed(2)}%</Badge>
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </Table>
                </div>
              )}
            </TabPane>

            {/* Ledger Tab */}
            <TabPane tabId="ledger">
              {loading ? (
                <div className="text-center py-4">
                  <Spinner color="primary" />
                </div>
              ) : ledgerEntries.length === 0 ? (
                <Alert color="info">No ledger entries found for traders under your broker.</Alert>
              ) : (
                <div className="table-responsive">
                  <Table hover>
                    <thead>
                      <tr>
                        <th>Entry ID</th>
                        <th>Type</th>
                        <th>Amount</th>
                        <th>Description</th>
                        <th>Date</th>
                      </tr>
                    </thead>
                    <tbody>
                      {ledgerEntries.map(entry => (
                        <tr key={entry.id}>
                          <td>#{entry.id}</td>
                          <td>{getLedgerBadge(entry.type)}</td>
                          <td>
                            <strong className={entry.type === 'DEBIT' ? 'text-danger' : 'text-success'}>
                              {entry.type === 'DEBIT' ? '-' : '+'} ₹{entry.amount.toFixed(2)}
                            </strong>
                          </td>
                          <td>{entry.description}</td>
                          <td>{new Date(entry.createdAt).toLocaleString()}</td>
                        </tr>
                      ))}
                    </tbody>
                  </Table>
                </div>
              )}
            </TabPane>
          </TabContent>
        </CardBody>
      </Card>

      {/* Educational disclaimer */}
      <Row className="mt-4">
        <Col>
          <Alert color="info">
            <strong>Educational Note:</strong> This is a simulated environment for learning trading concepts. All data is mock data and does
            not represent real trading activity or live market conditions.
          </Alert>
        </Col>
      </Row>
    </Container>
  );
};

export default BrokerPortfolio;
