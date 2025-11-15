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
  url,
  token,
  reconnectDelay = 5000,
  heartbeatIncoming = 10000,
  heartbeatOutgoing = 10000,
}: StompClientOptions = {}) => {
  // In dev the frontend runs on :9060 while the backend runs on :8080.
  // SockJS requires an absolute URL when crossing origins, so we detect
  // the dev server port and point directly at the Spring Boot backend.
  const resolvedUrl = url ?? (window.location.port === '9060' ? 'http://localhost:8080/ws' : '/ws');
  const headers: Record<string, string> = {};
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  return new Client({
    webSocketFactory: () => new SockJS(resolvedUrl),
    connectHeaders: headers,
    reconnectDelay,
    heartbeatIncoming,
    heartbeatOutgoing,
    debug() {},
  });
};
