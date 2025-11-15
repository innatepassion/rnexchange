import React, { useState, useCallback, useEffect } from 'react';
import { Container, Row, Col, Nav, NavItem, NavLink, TabContent, TabPane, Spinner, Alert } from 'reactstrap';
import { useAppSelector } from 'app/config/store';
import OrdersTrades from './orders-trades';
import PortfolioCash from './portfolio-cash';
import useTradingSubscription, { TradingWebSocketMessage } from './use-trading-subscription';

interface TraderDashboardProps {
  tradingAccountId?: number;
}

const TraderDashboard: React.FC<TraderDashboardProps> = ({ tradingAccountId: propAccountId }) => {
  const [activeTab, setActiveTab] = useState<'portfolio' | 'orders'>('portfolio');
  const [wsMessage, setWsMessage] = useState<TradingWebSocketMessage | null>(null);
  const [selectedAccountId, setSelectedAccountId] = useState<number | undefined>(propAccountId);

  const account = useAppSelector(state => state.authentication.account);

  // Use the provided account ID or fall back to a default
  useEffect(() => {
    if (propAccountId) {
      setSelectedAccountId(propAccountId);
    }
  }, [propAccountId]);

  const handleOrderUpdate = useCallback((message: TradingWebSocketMessage) => {
    console.log('Order update received:', message);
    setWsMessage(message);
  }, []);

  const handleExecutionUpdate = useCallback((message: TradingWebSocketMessage) => {
    console.log('Execution update received:', message);
    setWsMessage(message);
  }, []);

  const handlePositionUpdate = useCallback((message: TradingWebSocketMessage) => {
    console.log('Position update received:', message);
    setWsMessage(message);
  }, []);

  const connectionStatus = useTradingSubscription(selectedAccountId, handleOrderUpdate, handleExecutionUpdate, handlePositionUpdate);

  const getStatusBadgeColor = (status: string) => {
    switch (status) {
      case 'connected':
        return 'success';
      case 'connecting':
        return 'warning';
      case 'reconnecting':
        return 'warning';
      default:
        return 'danger';
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status) {
      case 'connected':
        return '● Connected';
      case 'connecting':
        return '⟳ Connecting...';
      case 'reconnecting':
        return '⟳ Reconnecting...';
      default:
        return '● Disconnected';
    }
  };

  if (!selectedAccountId) {
    return (
      <Container className="mt-5">
        <Alert color="warning">No trading account selected. Please select a trading account to proceed.</Alert>
      </Container>
    );
  }

  return (
    <Container fluid className="trader-dashboard py-4">
      <Row className="mb-4">
        <Col>
          <div className="d-flex justify-content-between align-items-center">
            <div>
              <h2>Trading Dashboard</h2>
              <small className="text-muted">Account ID: {selectedAccountId}</small>
            </div>
            <div>
              <div className={`badge bg-${getStatusBadgeColor(connectionStatus)}`}>{getStatusLabel(connectionStatus)}</div>
            </div>
          </div>
        </Col>
      </Row>

      <Row className="mb-4">
        <Col>
          <Nav tabs>
            <NavItem>
              <NavLink active={activeTab === 'portfolio'} onClick={() => setActiveTab('portfolio')} className="cursor-pointer">
                Portfolio & Cash
              </NavLink>
            </NavItem>
            <NavItem>
              <NavLink active={activeTab === 'orders'} onClick={() => setActiveTab('orders')} className="cursor-pointer">
                Orders & Trades
              </NavLink>
            </NavItem>
          </Nav>
        </Col>
      </Row>

      {wsMessage && (
        <Row className="mb-3">
          <Col>
            <Alert color="info" toggle={() => setWsMessage(null)}>
              <small>
                <strong>Real-time Update:</strong> {wsMessage.type} event at {new Date(wsMessage.timestamp).toLocaleTimeString()}
              </small>
            </Alert>
          </Col>
        </Row>
      )}

      <Row>
        <Col>
          <TabContent activeTab={activeTab}>
            <TabPane tabId="portfolio">
              <PortfolioCash tradingAccountId={selectedAccountId} onWebSocketMessage={wsMessage} />
            </TabPane>
            <TabPane tabId="orders">
              <OrdersTrades tradingAccountId={selectedAccountId} onWebSocketMessage={wsMessage} />
            </TabPane>
          </TabContent>
        </Col>
      </Row>

      <Row className="mt-5 mb-4">
        <Col>
          <div className="p-3 bg-light rounded border-start border-4 border-info">
            <strong>📚 Educational Note:</strong> This is a simulated trading environment for learning and training purposes only. All
            prices, executions, and portfolio values are based on mock data.
            <br />
            <small className="text-muted d-block mt-2">
              WebSocket connection delivers real-time updates of order fills, executions, and portfolio changes as they occur on the
              backend.
            </small>
          </div>
        </Col>
      </Row>
    </Container>
  );
};

export default TraderDashboard;
