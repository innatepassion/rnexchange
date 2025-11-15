import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { Storage } from 'react-jhipster';

import { createStompClient } from 'app/shared/websocket/stomp-client';
import type { IQuote } from 'app/shared/model/quote.model';

export type ConnectionListener = (status: 'connecting' | 'connected' | 'reconnecting' | 'disconnected') => void;

export class MarketDataWebSocketService {
  private client?: Client;
  private subscriptions: Map<string, StompSubscription> = new Map();
  private statusListener?: ConnectionListener;
  private reconnectTimeout?: number;

  private ensureClient() {
    if (this.client) {
      return this.client;
    }
    const token = Storage.local.get('jhi-authenticationToken') ?? Storage.session.get('jhi-authenticationToken');
    this.client = createStompClient({ token });
    return this.client;
  }

  connect(onStatusChange?: ConnectionListener) {
    if (onStatusChange) {
      this.statusListener = onStatusChange;
    }
    const client = this.ensureClient();
    client.onConnect = () => this.statusListener?.('connected');
    client.onStompError = frame => {
      const errorMessage = frame?.headers?.message?.toLowerCase() ?? '';
      if (errorMessage.includes('unauthorized')) {
        this.handleUnauthorized();
        return;
      }
      this.statusListener?.('reconnecting');
    };
    client.onWebSocketClose = () => this.statusListener?.('disconnected');
    client.onDisconnect = () => this.statusListener?.('disconnected');
    client.onWebSocketError = () => this.statusListener?.('reconnecting');
    if (!client.active) {
      this.statusListener?.('connecting');
      client.activate();
    }
  }

  disconnect() {
    if (this.reconnectTimeout) {
      window.clearTimeout(this.reconnectTimeout);
      this.reconnectTimeout = undefined;
    }
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
    this.subscriptions.clear();
    if (this.client) {
      this.client.deactivate();
      this.client = undefined;
    }
  }

  private handleUnauthorized() {
    this.statusListener?.('reconnecting');
    this.disconnect();
    this.client = undefined;
    this.reconnectTimeout = window.setTimeout(() => {
      this.reconnectTimeout = undefined;
      this.connect(this.statusListener);
    }, 1000);
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
        try {
          const parsed: IQuote = JSON.parse(message.body);
          onQuote(parsed);
        } catch (error) {
          // If the server ever sends a non-JSON payload (for example an HTML
          // error page), avoid crashing the UI with a SyntaxError and instead
          // log the bad frame for diagnostics.
          console.error('Failed to parse quote message', error, message.body);
        }
      });
      this.subscriptions.set(symbol, subscription);
    });
  }
}

export const marketDataWebSocketService = new MarketDataWebSocketService();
