import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { Storage } from 'react-jhipster';

import { createStompClient } from 'app/shared/websocket/stomp-client';
import type { IQuote } from 'app/shared/model/quote.model';

export type ConnectionListener = (status: 'connecting' | 'connected' | 'reconnecting' | 'disconnected') => void;

export class MarketDataWebSocketService {
  private client?: Client;
  private subscriptions: Map<string, StompSubscription> = new Map();

  private ensureClient() {
    if (this.client) {
      return this.client;
    }
    const token = Storage.local.get('jhi-authenticationToken') ?? Storage.session.get('jhi-authenticationToken');
    this.client = createStompClient({ token });
    return this.client;
  }

  connect(onStatusChange?: ConnectionListener) {
    const client = this.ensureClient();
    if (onStatusChange) {
      client.onConnect = () => onStatusChange('connected');
      client.onStompError = () => onStatusChange('reconnecting');
      client.onWebSocketClose = () => onStatusChange('disconnected');
      client.onDisconnect = () => onStatusChange('disconnected');
    }
    if (!client.active) {
      onStatusChange?.('connecting');
      client.activate();
    }
  }

  disconnect() {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
    this.subscriptions.clear();
    if (this.client) {
      this.client.deactivate();
      this.client = undefined;
    }
  }

  subscribe(symbols: string[], onQuote: (quote: IQuote) => void) {
    if (!this.client || !this.client.connected) {
      return;
    }
    const client = this.client;
    if (!client) {
      return;
    }
    const desired = new Set(symbols);

    this.subscriptions.forEach((subscription, symbol) => {
      if (!desired.has(symbol)) {
        subscription.unsubscribe();
        this.subscriptions.delete(symbol);
      }
    });

    symbols.forEach(symbol => {
      if (this.subscriptions.has(symbol)) {
        return;
      }
      const destination = `/topic/quotes/${symbol}`;
      const subscription = client.subscribe(destination, (message: IMessage) => {
        const parsed: IQuote = JSON.parse(message.body);
        onQuote(parsed);
      });
      this.subscriptions.set(symbol, subscription);
    });
  }
}

export const marketDataWebSocketService = new MarketDataWebSocketService();
