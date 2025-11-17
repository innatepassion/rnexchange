import React, { useState, useCallback, useEffect } from 'react';
import { Container, Row, Col, Nav, NavItem, NavLink, TabContent, TabPane, Spinner, Alert } from 'reactstrap';
import { useLocation } from 'react-router-dom';
import { useAppSelector } from 'app/config/store';
import OrdersTrades from './orders-trades';
import PortfolioCash from './portfolio-cash';
import useTradingSubscription, { TradingWebSocketMessage } from './use-trading-subscription';
import { getCurrentTradingAccount } from 'app/shared/api/trading.api';
import RoleHelpPanel from 'app/shared/components/RoleHelpPanel';
import SimulatedBanner from 'app/shared/components/SimulatedBanner';
import WebsocketConnectionBanner from 'app/shared/websocket/WebsocketConnectionBanner';

interface TraderDashboardProps {
  tradingAccountId?: number;
}

const TraderDashboard: React.FC<TraderDashboardProps> = ({ tradingAccountId: propAccountId }) => {
  const [activeTab, setActiveTab] = useState<'portfolio' | 'orders'>('portfolio');
  const [selectedAccountId, setSelectedAccountId] = useState<number | undefined>(propAccountId);
  const [isLoadingAccount, setIsLoadingAccount] = useState<boolean>(false);
  const [accountError, setAccountError] = useState<string | null>(null);

  const location = useLocation();
  const account = useAppSelector(state => state.authentication.account);

  // Use the provided account ID when coming via a deep link
  useEffect(() => {
    if (propAccountId) {
      setSelectedAccountId(propAccountId);
    }
  }, [propAccountId]);

  // Resolve default trading account for the logged-in trader when none is provided
  useEffect(() => {
    if (propAccountId || selectedAccountId) {
      return;
    }

    const fetchAccount = async () => {
      try {
        setIsLoadingAccount(true);
        setAccountError(null);
        const response = await getCurrentTradingAccount();
        const id = response.data.id;
        if (id) {
          setSelectedAccountId(id);
        } else {
          setAccountError('No trading account configured for this trader.');
        }
      } catch (e) {
        setAccountError('Failed to resolve trading account for current trader.');
      } finally {
        setIsLoadingAccount(false);
      }
    };

    fetchAccount();
  }, [propAccountId, selectedAccountId]);

  // Keep the active tab in sync with an optional ?tab= query parameter
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const tab = params.get('tab');
    if (tab === 'orders' || tab === 'portfolio') {
      setActiveTab(tab);
    }
  }, [location.search]);

  const handleOrderUpdate = useCallback((message: TradingWebSocketMessage) => {
    // Handle order updates
    if (message) {
      // Update UI state here
    }
  }, []);

  const handleExecutionUpdate = useCallback((message: TradingWebSocketMessage) => {
    // Handle execution updates
    if (message) {
      // Update UI state here
    }
  }, []);

  const handlePositionUpdate = useCallback((message: TradingWebSocketMessage) => {
    // Handle position updates
    if (message) {
      // Update UI state here
    }
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

  if (isLoadingAccount) {
    return (
      <Container className="mt-5 text-center">
        <Spinner size="sm" className="me-2" /> Resolving trading account...
      </Container>
    );
  }

  if (!selectedAccountId) {
    return (
      <Container className="mt-5">
        <Alert color="warning">
          {accountError || 'No trading account selected. Please contact support to configure a trading account for this user.'}
        </Alert>
      </Container>
    );
  }

  return (
    <Container fluid className="trader-dashboard py-4">
      {/* T021 [US1]: Persistent SIMULATED banner */}
      <SimulatedBanner />
      {/* T020 [US1]: WebSocket connection status banner */}
      <WebsocketConnectionBanner tradingAccountId={selectedAccountId} />
      <Row className="mb-4">
        <Col>
          <div className="d-flex justify-content-between align-items-center">
            <div>
              <h2>Trading Dashboard</h2>
              <small className="text-muted">Account ID: {selectedAccountId}</small>
            </div>
            <div className="d-flex align-items-center gap-3">
              <RoleHelpPanel />
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

      <Row>
        <Col>
          <TabContent activeTab={activeTab}>
            <TabPane tabId="portfolio">
              <PortfolioCash tradingAccountId={selectedAccountId} />
            </TabPane>
            <TabPane tabId="orders">
              <OrdersTrades tradingAccountId={selectedAccountId} />
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
