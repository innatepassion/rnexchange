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

      <Alert color="info" className="mb-4">
        <strong>ℹ️ Back-Office Oversight:</strong> This dashboard shows real-time trading activity and balances for all traders under your
        broker. Use this view to monitor risk, confirm trades, and ensure compliance. <strong>Note:</strong> This is a training environment
        with simulated data.
      </Alert>

      <Card>
        <CardHeader>
          <Nav tabs>
            <NavItem>
              <NavLink
                active={activeTab === 'orders'}
                onClick={() => setActiveTab('orders')}
                style={{ cursor: 'pointer' }}
                title="All orders placed by your traders (filled, rejected, pending, etc.)"
              >
                <FontAwesomeIcon icon={faExchangeAlt} className="me-2" />
                Orders ({orders.length})
              </NavLink>
            </NavItem>
            <NavItem>
              <NavLink
                active={activeTab === 'positions'}
                onClick={() => setActiveTab('positions')}
                style={{ cursor: 'pointer' }}
                title="Open positions held by your traders (holdings, average cost, MTM)"
              >
                <FontAwesomeIcon icon={faChartLine} className="me-2" />
                Positions ({positions.length})
              </NavLink>
            </NavItem>
            <NavItem>
              <NavLink
                active={activeTab === 'ledger'}
                onClick={() => setActiveTab('ledger')}
                style={{ cursor: 'pointer' }}
                title="All cash debits and credits from trading activity"
              >
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
                        <th title="Unique order identifier">Order ID</th>
                        <th title="Stock symbol (e.g., RELIANCE)">Instrument</th>
                        <th title="BUY (increasing holdings) or SELL (decreasing holdings)">Side</th>
                        <th title="MARKET (execute now) or LIMIT (at specified price)">Type</th>
                        <th title="Number of units">Qty</th>
                        <th title="Maximum/minimum price for LIMIT orders (empty for MARKET)">Limit Price</th>
                        <th title="Actual price the order filled at">Execution Price</th>
                        <th title="Order status: FILLED, REJECTED, ACCEPTED, NEW">Status</th>
                        <th title="When the order was submitted">Created</th>
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
                        <th title="Stock symbol">Instrument</th>
                        <th title="Number of units held">Qty</th>
                        <th title="Weighted average purchase price per unit">Avg Cost</th>
                        <th title="Current market price (mark-to-market)">Last Price</th>
                        <th title="Current profit/loss at market price: (Last Price - Avg Cost) × Qty">MTM Value</th>
                        <th title="MTM profit/loss as a percentage">MTM %</th>
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
                        <th title="Unique ledger entry identifier">Entry ID</th>
                        <th title="DEBIT (cash out) or CREDIT (cash in)">Type</th>
                        <th title="Amount in INR">Amount</th>
                        <th title="Detailed transaction description (symbol, side, quantity, price, P&L)">Description</th>
                        <th title="When the transaction occurred">Date</th>
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
          <Card className="bg-light">
            <CardBody>
              <p className="mb-2">
                <strong>📚 Training Environment Disclaimer:</strong> This is a simulated trading platform designed for learning. All market
                prices, fills, and portfolio values are mock data. The following are training-only concepts:
              </p>
              <ul className="mb-2">
                <li>
                  <strong>Immediate Fills:</strong> MARKET orders execute instantly in this system. Real markets depend on liquidity and
                  current bid/ask spreads.
                </li>
                <li>
                  <strong>Fixed Fees:</strong> Flat ₹25 trading fee for all orders. Real brokers use percentage-based or tiered fees.
                </li>
                <li>
                  <strong>CASH-Only Scope (FR-014):</strong> No margin trading, short selling, or derivatives. This covers the M2 MVP scope
                  only.
                </li>
                <li>
                  <strong>No Real Financial Impact:</strong> Profits and losses shown here do not represent real money or portfolio
                  performance.
                </li>
              </ul>
              <p className="mb-0">
                <strong>Learning Objective:</strong> Use this platform to understand order workflows, position tracking, P&L calculations,
                and back-office oversight. Then apply these concepts to real trading in a live environment (always with proper risk
                management).
              </p>
            </CardBody>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default BrokerPortfolio;
