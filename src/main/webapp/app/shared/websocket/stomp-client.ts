import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export interface StompClientOptions {
  url?: string;
  token?: string;
  reconnectDelay?: number;
  heartbeatIncoming?: number;
  heartbeatOutgoing?: number;
}

export const createStompClient = ({
  url = '/ws',
  token,
  reconnectDelay = 5000,
  heartbeatIncoming = 10000,
  heartbeatOutgoing = 10000,
}: StompClientOptions = {}) => {
  const headers: Record<string, string> = {};
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  return new Client({
    webSocketFactory: () => new SockJS(url),
    connectHeaders: headers,
    reconnectDelay,
    heartbeatIncoming,
    heartbeatOutgoing,
    debug() {},
  });
};
