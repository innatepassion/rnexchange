import { useEffect, useState, useCallback, useRef } from 'react';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { Storage } from 'react-jhipster';
import { createStompClient } from 'app/shared/websocket/stomp-client';

export type TradingEventType = 'order' | 'execution' | 'position';
export type ConnectionListener = (status: 'connecting' | 'connected' | 'reconnecting' | 'disconnected') => void;

export interface TradingWebSocketMessage {
  type: TradingEventType;
  data: any;
  timestamp: string;
}

export const useTradingSubscription = (
  tradingAccountId: number | string | undefined,
  onOrderUpdate?: (message: TradingWebSocketMessage) => void,
  onExecutionUpdate?: (message: TradingWebSocketMessage) => void,
  onPositionUpdate?: (message: TradingWebSocketMessage) => void,
) => {
  const [connectionStatus, setConnectionStatus] = useState<'connecting' | 'connected' | 'reconnecting' | 'disconnected'>('disconnected');
  const clientRef = useRef<Client | undefined>();
  const subscriptionsRef = useRef<Map<string, StompSubscription>>(new Map());

  const ensureClient = useCallback(() => {
    if (clientRef.current) {
      return clientRef.current;
    }
    const token = Storage.local.get('jhi-authenticationToken') ?? Storage.session.get('jhi-authenticationToken');
    clientRef.current = createStompClient({ token });
    return clientRef.current;
  }, []);

  const connect = useCallback(() => {
    const client = ensureClient();
    client.onConnect = () => setConnectionStatus('connected');
    client.onStompError = frame => {
      const errorMessage = frame?.headers?.message?.toLowerCase() ?? '';
      if (errorMessage.includes('unauthorized')) {
        handleUnauthorized();
        return;
      }
      setConnectionStatus('reconnecting');
    };
    client.onWebSocketClose = () => setConnectionStatus('disconnected');
    client.onDisconnect = () => setConnectionStatus('disconnected');
    client.onWebSocketError = () => setConnectionStatus('reconnecting');

    if (!client.active) {
      setConnectionStatus('connecting');
      client.activate();
    }
  }, [ensureClient]);

  const handleUnauthorized = useCallback(() => {
    setConnectionStatus('reconnecting');
    disconnect();
    clientRef.current = undefined;
    setTimeout(() => {
      connect();
    }, 1000);
  }, [connect]);

  const disconnect = useCallback(() => {
    subscriptionsRef.current.forEach(subscription => subscription.unsubscribe());
    subscriptionsRef.current.clear();
    if (clientRef.current) {
      clientRef.current.deactivate();
    }
  }, []);

  const subscribe = useCallback(() => {
    if (!tradingAccountId || !clientRef.current?.connected) {
      return;
    }

    const client = clientRef.current;

    // Subscribe to order updates
    if (onOrderUpdate) {
      const orderDestination = `/topic/orders/${tradingAccountId}`;
      const orderSub = client.subscribe(orderDestination, (message: IMessage) => {
        try {
          const parsed: TradingWebSocketMessage = JSON.parse(message.body);
          onOrderUpdate(parsed);
        } catch (error) {
          console.error('Failed to parse order message', error, message.body);
        }
      });
      subscriptionsRef.current.set(orderDestination, orderSub);
    }

    // Subscribe to execution updates
    if (onExecutionUpdate) {
      const executionDestination = `/topic/executions/${tradingAccountId}`;
      const executionSub = client.subscribe(executionDestination, (message: IMessage) => {
        try {
          const parsed: TradingWebSocketMessage = JSON.parse(message.body);
          onExecutionUpdate(parsed);
        } catch (error) {
          console.error('Failed to parse execution message', error, message.body);
        }
      });
      subscriptionsRef.current.set(executionDestination, executionSub);
    }

    // Subscribe to position updates
    if (onPositionUpdate) {
      const positionDestination = `/topic/positions/${tradingAccountId}`;
      const positionSub = client.subscribe(positionDestination, (message: IMessage) => {
        try {
          const parsed: TradingWebSocketMessage = JSON.parse(message.body);
          onPositionUpdate(parsed);
        } catch (error) {
          console.error('Failed to parse position message', error, message.body);
        }
      });
      subscriptionsRef.current.set(positionDestination, positionSub);
    }
  }, [tradingAccountId, onOrderUpdate, onExecutionUpdate, onPositionUpdate]);

  // Initial connection setup
  useEffect(() => {
    connect();
    return () => {
      disconnect();
    };
  }, [connect, disconnect]);

  // Subscribe to topics when connection is established
  useEffect(() => {
    if (connectionStatus === 'connected' && tradingAccountId) {
      subscribe();
    }
  }, [connectionStatus, tradingAccountId, subscribe]);

  return connectionStatus;
};

export default useTradingSubscription;
