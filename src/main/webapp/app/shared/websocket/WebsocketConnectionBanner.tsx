import React, { useEffect, useState } from 'react';
import { Alert, Spinner } from 'reactstrap';
import { useTradingSubscription } from 'app/modules/trader/use-trading-subscription';

export type ConnectionStatus = 'connecting' | 'connected' | 'reconnecting' | 'disconnected';

interface WebsocketConnectionBannerProps {
  tradingAccountId?: number | string;
  /**
   * T020 [US1]: Optional callback for logging connection status changes
   */
  onStatusChange?: (status: ConnectionStatus) => void;
}

/**
 * T020 [US1]: WebSocket Connection Banner Component
 *
 * Displays a banner showing the current WebSocket connection status for real-time trading updates.
 * Provides clear guidance during transient connection issues (disconnects, retries).
 *
 * Features:
 * - Shows connection status (connecting, connected, reconnecting, disconnected)
 * - Logs status changes for debugging
 * - Provides user-friendly messages during connection issues
 * - Auto-hides when connected
 */
const WebsocketConnectionBanner: React.FC<WebsocketConnectionBannerProps> = ({ tradingAccountId, onStatusChange }) => {
  const [status, setStatus] = useState<ConnectionStatus>('disconnected');
  const [retryCount, setRetryCount] = useState(0);
  const [lastError, setLastError] = useState<string | null>(null);

  // T020 [US1]: Subscribe to trading WebSocket updates to monitor connection status
  const connectionStatus = useTradingSubscription(
    tradingAccountId,
    undefined, // onOrderUpdate
    undefined, // onExecutionUpdate
    undefined, // onPositionUpdate
  );

  useEffect(() => {
    setStatus(connectionStatus);

    // T020 [US1]: Log connection status changes for debugging
    if (onStatusChange) {
      onStatusChange(connectionStatus);
    }

    // T020 [US1]: Log to console for debugging (can be disabled in production)
    if (process.env.NODE_ENV !== 'production') {
      console.warn(`[WebSocket] Connection status: ${connectionStatus}`, {
        tradingAccountId,
        timestamp: new Date().toISOString(),
      });
    }

    // Track retry count for reconnecting state
    if (connectionStatus === 'reconnecting') {
      setRetryCount(prev => prev + 1);
      setLastError('Connection lost. Attempting to reconnect...');
    } else if (connectionStatus === 'connected') {
      setRetryCount(0);
      setLastError(null);
    } else if (connectionStatus === 'disconnected') {
      setLastError('Real-time updates unavailable. Please refresh the page if this persists.');
    }
  }, [connectionStatus, tradingAccountId, onStatusChange]);

  // T020 [US1]: Don't show banner when connected (no issues)
  if (status === 'connected') {
    return null;
  }

  // T020 [US1]: Determine alert color and message based on status
  let alertColor: 'warning' | 'danger' | 'info' = 'warning';
  let message = '';
  let showSpinner = false;

  switch (status) {
    case 'connecting':
      alertColor = 'info';
      message = 'Connecting to real-time updates...';
      showSpinner = true;
      break;
    case 'reconnecting':
      alertColor = 'warning';
      message = `Reconnecting to real-time updates${retryCount > 1 ? ` (attempt ${retryCount})` : ''}...`;
      showSpinner = true;
      break;
    case 'disconnected':
      alertColor = 'danger';
      message = 'Real-time updates disconnected. Some features may not update automatically.';
      showSpinner = false;
      break;
    default:
      return null;
  }

  return (
    <Alert color={alertColor} className="mb-2" data-cy="websocket-connection-banner">
      <div className="d-flex align-items-center">
        {showSpinner && <Spinner size="sm" className="me-2" />}
        <div className="flex-grow-1">
          <strong>WebSocket Status:</strong> {message}
          {lastError && (
            <div className="small mt-1">
              <em>{lastError}</em>
            </div>
          )}
          {status === 'disconnected' && (
            <div className="small mt-1">
              <strong>Tip:</strong> If this persists, try refreshing the page or check your network connection.
            </div>
          )}
        </div>
      </div>
    </Alert>
  );
};

export default WebsocketConnectionBanner;
