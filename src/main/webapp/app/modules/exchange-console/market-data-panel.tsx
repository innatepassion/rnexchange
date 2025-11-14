import React, { useEffect, useState } from 'react';
import { Button, Table, Badge, UncontrolledTooltip } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faInfoCircle, faPlay, faStop, faCircle } from '@fortawesome/free-solid-svg-icons';
import axios from 'axios';
import { toast } from 'react-toastify';
import './market-data-panel.scss';

interface ExchangeStatus {
  exchangeCode: string;
  state: 'RUNNING' | 'STOPPED' | 'HOLIDAY';
  lastTickTime: string | null;
  ticksPerSecond: number;
  activeInstruments: number;
}

interface FeedStatus {
  globalState: 'RUNNING' | 'STOPPED';
  startedAt: string | null;
  exchanges: ExchangeStatus[];
}

const MarketDataPanel: React.FC = () => {
  const [feedStatus, setFeedStatus] = useState<FeedStatus | null>(null);
  const [loading, setLoading] = useState(false);

  const fetchStatus = async () => {
    try {
      const response = await axios.get<FeedStatus>('/api/marketdata/mock/status');
      setFeedStatus(response.data);
    } catch (error) {
      console.error('Failed to fetch feed status:', error);
      toast.error('Failed to fetch market data feed status');
    }
  };

  useEffect(() => {
    fetchStatus();
    const interval = setInterval(fetchStatus, 2000); // Poll every 2 seconds
    return () => clearInterval(interval);
  }, []);

  const handleStart = async () => {
    setLoading(true);
    try {
      const response = await axios.post<FeedStatus>('/api/marketdata/mock/start');
      setFeedStatus(response.data);
      toast.success('Market data feed started successfully');
    } catch (error) {
      console.error('Failed to start feed:', error);
      toast.error('Failed to start market data feed');
    } finally {
      setLoading(false);
    }
  };

  const handleStop = async () => {
    setLoading(true);
    try {
      const response = await axios.post<FeedStatus>('/api/marketdata/mock/stop');
      setFeedStatus(response.data);
      toast.success('Market data feed stopped successfully');
    } catch (error) {
      console.error('Failed to stop feed:', error);
      toast.error('Failed to stop market data feed');
    } finally {
      setLoading(false);
    }
  };

  const formatTime = (timestamp: string | null): string => {
    if (!timestamp) return 'N/A';
    return new Date(timestamp).toLocaleTimeString();
  };

  const getStateBadgeColor = (state: string) => {
    switch (state) {
      case 'RUNNING':
        return 'success';
      case 'STOPPED':
        return 'secondary';
      case 'HOLIDAY':
        return 'warning';
      default:
        return 'secondary';
    }
  };

  if (!feedStatus) {
    return <div>Loading...</div>;
  }

  return (
    <div className="market-data-panel">
      <div className="panel-header">
        <h3>
          Market Data Feed Control
          <FontAwesomeIcon
            id="panel-info-tooltip"
            icon={faInfoCircle}
            className="ms-2 text-muted info-icon"
            role="button"
            aria-label="info"
          />
          <UncontrolledTooltip placement="right" target="panel-info-tooltip">
            This panel controls the simulated market data feed for training purposes. The feed generates realistic price movements for all
            active instruments across NSE, BSE, and MCX exchanges.
          </UncontrolledTooltip>
        </h3>
      </div>

      <div className="status-section mb-4">
        <div className="d-flex align-items-center justify-content-between">
          <div>
            <h5>Global Feed State</h5>
            <h4>
              <Badge color={getStateBadgeColor(feedStatus.globalState)} className="me-2">
                {feedStatus.globalState}
              </Badge>
              {feedStatus.startedAt && <small className="text-muted">Started at {formatTime(feedStatus.startedAt)}</small>}
            </h4>
          </div>
          <div className="control-buttons">
            <Button
              color="success"
              onClick={handleStart}
              disabled={loading || feedStatus.globalState === 'RUNNING'}
              className="me-2"
              aria-label="start feed"
            >
              <FontAwesomeIcon icon={faPlay} className="me-2" />
              Start Feed
            </Button>
            <Button color="danger" onClick={handleStop} disabled={loading || feedStatus.globalState === 'STOPPED'} aria-label="stop feed">
              <FontAwesomeIcon icon={faStop} className="me-2" />
              Stop Feed
            </Button>
          </div>
        </div>
      </div>

      <div className="exchanges-section">
        <h5>
          Per-Exchange Metrics
          <FontAwesomeIcon
            id="metrics-info-tooltip"
            icon={faInfoCircle}
            className="ms-2 text-muted info-icon"
            role="button"
            aria-label="info"
          />
          <UncontrolledTooltip placement="right" target="metrics-info-tooltip">
            Real-time metrics for each exchange. Ticks/sec shows the rate of price updates. HOLIDAY state indicates the exchange is closed
            due to a market holiday.
          </UncontrolledTooltip>
        </h5>

        <Table striped bordered hover>
          <thead>
            <tr>
              <th>
                Exchange
                <FontAwesomeIcon
                  id="exchange-col-tooltip"
                  icon={faInfoCircle}
                  className="ms-1 text-muted small info-icon"
                  role="button"
                  aria-label="info"
                />
                <UncontrolledTooltip placement="top" target="exchange-col-tooltip">
                  Exchange code (NSE, BSE, MCX)
                </UncontrolledTooltip>
              </th>
              <th>State</th>
              <th>
                Ticks/sec
                <FontAwesomeIcon
                  id="ticks-col-tooltip"
                  icon={faInfoCircle}
                  className="ms-1 text-muted small info-icon"
                  role="button"
                  aria-label="info"
                />
                <UncontrolledTooltip placement="top" target="ticks-col-tooltip">
                  Number of price updates per second (5-second moving average)
                </UncontrolledTooltip>
              </th>
              <th>
                Last Tick
                <FontAwesomeIcon
                  id="last-tick-col-tooltip"
                  icon={faInfoCircle}
                  className="ms-1 text-muted small info-icon"
                  role="button"
                  aria-label="info"
                />
                <UncontrolledTooltip placement="top" target="last-tick-col-tooltip">
                  Time of the most recent price update
                </UncontrolledTooltip>
              </th>
              <th>Active Instruments</th>
            </tr>
          </thead>
          <tbody>
            {feedStatus.exchanges.map(exchange => (
              <tr key={exchange.exchangeCode}>
                <td>
                  <strong>{exchange.exchangeCode}</strong>
                </td>
                <td>
                  <Badge color={getStateBadgeColor(exchange.state)}>{exchange.state}</Badge>
                </td>
                <td className={exchange.ticksPerSecond > 0 ? 'text-success' : 'text-muted'}>
                  {exchange.ticksPerSecond}
                  {exchange.ticksPerSecond > 0 && (
                    <FontAwesomeIcon icon={faCircle} className="ms-2 blink-icon" style={{ fontSize: '0.5rem' }} />
                  )}
                </td>
                <td>{formatTime(exchange.lastTickTime)}</td>
                <td>{exchange.activeInstruments}</td>
              </tr>
            ))}
          </tbody>
        </Table>
      </div>

      <div className="alert alert-info mt-3">
        <FontAwesomeIcon icon={faInfoCircle} className="me-2" />
        <strong>Simulated Data:</strong> This feed generates mock market data for training and testing purposes only. Prices do not reflect
        real market conditions.
      </div>
    </div>
  );
};

export default MarketDataPanel;
