# WebSocket Topics Contract: Mock Market Data

**Feature**: 002-mock-market-data  
**Protocol**: STOMP over WebSocket (SockJS)  
**Endpoint**: `ws://localhost:8080/ws` (development)  
**Authentication**: JWT token in `Sec-WebSocket-Protocol` header or `token` query parameter

---

## Overview

This document defines the WebSocket topic structure and message formats for real-time market data broadcasting. Clients subscribe to specific instrument symbols to receive quote and bar updates.

**Key Characteristics**:

- **Unidirectional**: Server → Client only (no client commands via WebSocket)
- **Topic-Based**: Each instrument has dedicated `/topic/quotes/{symbol}` and `/topic/bars/{symbol}` channels
- **Broadcast**: All subscribers to a topic receive the same message
- **JSON Serialization**: All messages are JSON-encoded

---

## Connection Handshake

### 1. Establish WebSocket Connection

**URL**: `ws://localhost:8080/ws`  
**Subprotocol**: `v12.stomp` (STOMP version 1.2)

**Authentication Methods**:

#### Option A: Sec-WebSocket-Protocol Header (Preferred)

```javascript
const socket = new SockJS('http://localhost:8080/ws', null, {
  transports: ['websocket', 'xhr-polling'],
});

const stompClient = Stomp.over(socket);
stompClient.connect({ Authorization: 'Bearer ' + jwtToken }, onConnect, onError);
```

#### Option B: Query Parameter

```javascript
const socket = new SockJS(`http://localhost:8080/ws?token=${jwtToken}`);
const stompClient = Stomp.over(socket);
stompClient.connect({}, onConnect, onError);
```

### 2. STOMP CONNECT Frame

```text
CONNECT
Authorization:Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
accept-version:1.2
heart-beat:10000,10000

^@
```

**Headers**:

- `Authorization`: JWT token (validated by Spring Security)
- `accept-version`: STOMP version (1.2)
- `heart-beat`: Client/server heartbeat intervals in milliseconds

### 3. STOMP CONNECTED Frame (Server Response)

```text
CONNECTED
version:1.2
heart-beat:10000,10000
session:abc123xyz

^@
```

---

## Topic Subscription

### Quote Topic

**Topic Pattern**: `/topic/quotes/{symbol}`  
**Purpose**: Receive real-time tick updates for a specific instrument  
**Frequency**: Every 500-1000ms when feed is running  
**Roles**: `TRADER`, `BROKER_ADMIN`, `EXCHANGE_OPERATOR`

#### STOMP SUBSCRIBE Frame

```text
SUBSCRIBE
id:sub-1
destination:/topic/quotes/RELIANCE
ack:auto

^@
```

**Headers**:

- `id`: Unique subscription ID (client-generated)
- `destination`: Topic path with instrument symbol
- `ack`: Auto-acknowledge (no manual ACK required)

#### MESSAGE Frame (Server → Client)

```text
MESSAGE
destination:/topic/quotes/RELIANCE
message-id:abc-123
subscription:sub-1
content-type:application/json
content-length:187

{"symbol":"RELIANCE","lastPrice":2485.30,"open":2475.00,"change":10.30,"changePercent":0.4162,"volume":1523400,"timestamp":"2025-11-13T10:32:15.123Z"}
^@
```

#### Payload Schema: QuoteDTO

```json
{
  "symbol": "RELIANCE",
  "lastPrice": 2485.3,
  "open": 2475.0,
  "change": 10.3,
  "changePercent": 0.4162,
  "volume": 1523400,
  "timestamp": "2025-11-13T10:32:15.123Z"
}
```

| Field           | Type              | Description                                  | Example                      |
| --------------- | ----------------- | -------------------------------------------- | ---------------------------- |
| `symbol`        | string            | Instrument symbol (matches subscription)     | `"RELIANCE"`                 |
| `lastPrice`     | number            | Current simulated price                      | `2485.30`                    |
| `open`          | number            | Session open price (set at feed start)       | `2475.00`                    |
| `change`        | number            | Absolute change from open (lastPrice - open) | `10.30`                      |
| `changePercent` | number            | Percentage change ((change / open) × 100)    | `0.4162`                     |
| `volume`        | integer           | Cumulative volume for session                | `1523400`                    |
| `timestamp`     | string (ISO 8601) | Tick generation time (UTC)                   | `"2025-11-13T10:32:15.123Z"` |

**Notes**:

- Prices are in INR with 2 decimal precision
- Volume is cumulative from session start (resets on feed restart)
- `change` and `changePercent` are computed server-side for consistency

---

### Bar Topic

**Topic Pattern**: `/topic/bars/{symbol}`  
**Purpose**: Receive 1-minute OHLC bar summaries for a specific instrument  
**Frequency**: Every 60 seconds when feed is running  
**Roles**: `TRADER`, `BROKER_ADMIN`, `EXCHANGE_OPERATOR`

#### STOMP SUBSCRIBE Frame

```text
SUBSCRIBE
id:sub-2
destination:/topic/bars/GOLD_FUT_DEC25
ack:auto

^@
```

#### MESSAGE Frame (Server → Client)

```text
MESSAGE
destination:/topic/bars/GOLD_FUT_DEC25
message-id:def-456
subscription:sub-2
content-type:application/json
content-length:152

{"symbol":"GOLD_FUT_DEC25","open":62500.00,"high":62785.50,"low":62410.00,"close":62650.00,"volume":8520,"timestamp":"2025-11-13T10:33:00.000Z"}
^@
```

#### Payload Schema: BarDTO

```json
{
  "symbol": "GOLD_FUT_DEC25",
  "open": 62500.0,
  "high": 62785.5,
  "low": 62410.0,
  "close": 62650.0,
  "volume": 8520,
  "timestamp": "2025-11-13T10:33:00.000Z"
}
```

| Field       | Type              | Description                                     | Example                      |
| ----------- | ----------------- | ----------------------------------------------- | ---------------------------- |
| `symbol`    | string            | Instrument symbol (matches subscription)        | `"GOLD_FUT_DEC25"`           |
| `open`      | number            | Session open price (not 1-minute bar open)      | `62500.00`                   |
| `high`      | number            | Session high (highest tick since feed start)    | `62785.50`                   |
| `low`       | number            | Session low (lowest tick since feed start)      | `62410.00`                   |
| `close`     | number            | Current last price (same as QuoteDTO.lastPrice) | `62650.00`                   |
| `volume`    | integer           | Cumulative session volume                       | `8520`                       |
| `timestamp` | string (ISO 8601) | Bar generation time (every 60s)                 | `"2025-11-13T10:33:00.000Z"` |

**Important**: Per FR-006 and clarifications, OHLC values are **session-level** (not per-minute windows). The bar is published every 60 seconds but reflects cumulative session data.

---

## Subscription Limits & Best Practices

### Rate Limits

| Limit                        | Value                                    | Enforcement                                   |
| ---------------------------- | ---------------------------------------- | --------------------------------------------- |
| Max subscriptions per client | 50 symbols                               | Server-side (returns ERROR frame if exceeded) |
| Max quote frequency          | 1 message per 500ms per symbol           | Server throttles at broadcast                 |
| Max bar frequency            | 1 message per 60s per symbol             | Fixed schedule                                |
| Heartbeat interval           | 10s client → server, 10s server → client | STOMP protocol                                |

### Subscription Management

**Pattern**: Subscribe only to symbols in active watchlist

```typescript
// Good: Subscribe to watchlist symbols only
const watchlistSymbols = ['RELIANCE', 'TCS', 'INFY'];
watchlistSymbols.forEach(symbol => {
  stompClient.subscribe(`/topic/quotes/${symbol}`, handleQuote);
});

// Bad: Subscribe to all instruments (violates 50-symbol limit)
allInstruments.forEach(symbol => {
  stompClient.subscribe(`/topic/quotes/${symbol}`, handleQuote); // DON'T DO THIS
});
```

**Unsubscribe on watchlist change**:

```typescript
// When removing symbol from watchlist:
stompClient.unsubscribe(subscriptionId);

// When adding symbol to watchlist:
const newSub = stompClient.subscribe(`/topic/quotes/${newSymbol}`, handleQuote);
```

### Reconnection Strategy

**Exponential Backoff**: Use `@stomp/stompjs` built-in reconnection

```typescript
const client = new Client({
  brokerURL: 'ws://localhost:8080/ws',
  reconnectDelay: 5000, // Initial delay: 5s
  connectionTimeout: 10000, // Timeout after 10s
  heartbeatIncoming: 10000,
  heartbeatOutgoing: 10000,

  onConnect: () => {
    console.log('WebSocket connected');
    // Re-subscribe to watchlist symbols
    resubscribeToWatchlist();
  },

  onDisconnect: () => {
    console.log('WebSocket disconnected');
    // Show "Disconnected" badge in UI
  },

  onStompError: frame => {
    console.error('STOMP error', frame);
    // Show error message in UI
  },
});
```

### Stale Data Detection

**Problem**: WebSocket reports "connected" but no messages arrive (network partition, server overload)

**Solution**: Track last message timestamp per symbol

```typescript
const lastMessageTime = new Map<string, number>();

function handleQuote(message: IMessage) {
  const quote = JSON.parse(message.body);
  lastMessageTime.set(quote.symbol, Date.now());
  // Update UI with quote
}

// Check for stale data every 5 seconds
setInterval(() => {
  const now = Date.now();
  watchlistSymbols.forEach(symbol => {
    const lastTime = lastMessageTime.get(symbol) || 0;
    if (now - lastTime > 10000) {
      // No message for 10s
      showStaleIndicator(symbol);
    }
  });
}, 5000);
```

---

## Error Handling

### ERROR Frame (Server → Client)

Sent when subscription fails (e.g., invalid topic, exceeded limit)

```text
ERROR
message:Subscription limit exceeded
content-length:84

Maximum 50 subscriptions per client. Current subscriptions: 50. Requested: /topic/quotes/TCS
^@
```

**Common Error Scenarios**:

| Error                         | Cause                               | Resolution                               |
| ----------------------------- | ----------------------------------- | ---------------------------------------- |
| `Unauthorized`                | Invalid/expired JWT token           | Refresh token and reconnect              |
| `Subscription limit exceeded` | >50 active subscriptions            | Unsubscribe from unused symbols          |
| `Topic does not exist`        | Subscribed to invalid topic pattern | Verify symbol exists in Instrument table |
| `Heartbeat timeout`           | No heartbeat received for 20s       | Automatic reconnection triggered         |

---

## Security Considerations

### Role-Based Access Control

**All Topics**: Require authenticated user (TRADER, BROKER_ADMIN, or EXCHANGE_OPERATOR)

**No Topic-Level Authorization**: Any authenticated user can subscribe to any symbol (rationale: market data is public within exchange ecosystem)

**Future Enhancement** (Post-MVP): Restrict topics based on user's allowed exchanges (e.g., TRADER at Broker A can only subscribe to NSE/BSE, not MCX)

### JWT Token Lifecycle

**Token Expiry**: Access tokens expire after 15 minutes (per constitution)

**Handling Expiry**:

1. Server sends ERROR frame with `message: Unauthorized`
2. Client detects error
3. Client requests new token via `/api/authenticate` (refresh token flow)
4. Client reconnects WebSocket with new token

**Implementation**:

```typescript
client.onStompError = frame => {
  if (frame.headers.message === 'Unauthorized') {
    // Token expired, refresh and reconnect
    refreshToken().then(newToken => {
      client.connectHeaders = { Authorization: `Bearer ${newToken}` };
      client.activate();
    });
  }
};
```

---

## Example Client Implementation

### React Hook: useMarketDataSubscription

```typescript
import { useEffect, useState, useRef } from 'react';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

interface IQuote {
  symbol: string;
  lastPrice: number;
  open: number;
  change: number;
  changePercent: number;
  volume: number;
  timestamp: string;
}

type ConnectionStatus = 'connecting' | 'connected' | 'disconnected';

export const useMarketDataSubscription = (symbols: string[], onQuote: (quote: IQuote) => void): ConnectionStatus => {
  const [status, setStatus] = useState<ConnectionStatus>('connecting');
  const clientRef = useRef<Client | null>(null);
  const subscriptionsRef = useRef<Map<string, any>>(new Map());

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      connectHeaders: {
        Authorization: `Bearer ${getJWTToken()}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,

      onConnect: () => {
        setStatus('connected');

        // Subscribe to quote topics
        symbols.forEach(symbol => {
          const subscription = client.subscribe(`/topic/quotes/${symbol}`, (message: IMessage) => {
            const quote: IQuote = JSON.parse(message.body);
            onQuote(quote);
          });
          subscriptionsRef.current.set(symbol, subscription);
        });
      },

      onDisconnect: () => {
        setStatus('disconnected');
        subscriptionsRef.current.clear();
      },

      onStompError: frame => {
        console.error('STOMP error:', frame);
        setStatus('disconnected');
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      // Cleanup: unsubscribe and disconnect
      subscriptionsRef.current.forEach(sub => sub.unsubscribe());
      client.deactivate();
    };
  }, [symbols.join(',')]); // Re-subscribe when symbols change

  return status;
};
```

### Usage in Market Watch Component

```typescript
const MarketWatch: React.FC = () => {
  const [quotes, setQuotes] = useState<Map<string, IQuote>>(new Map());
  const watchlistSymbols = ['RELIANCE', 'TCS', 'INFY'];

  const handleQuote = (quote: IQuote) => {
    setQuotes(prev => new Map(prev).set(quote.symbol, quote));
  };

  const connectionStatus = useMarketDataSubscription(watchlistSymbols, handleQuote);

  return (
    <div>
      <div className="status-badge">{connectionStatus}</div>
      <table>
        <thead>
          <tr>
            <th>Symbol</th>
            <th>LTP</th>
            <th>Change %</th>
            <th>Volume</th>
          </tr>
        </thead>
        <tbody>
          {watchlistSymbols.map(symbol => {
            const quote = quotes.get(symbol);
            return (
              <tr key={symbol}>
                <td>{symbol}</td>
                <td>{quote?.lastPrice ?? '-'}</td>
                <td className={quote && quote.change >= 0 ? 'positive' : 'negative'}>
                  {quote?.changePercent.toFixed(2) ?? '-'}%
                </td>
                <td>{quote?.volume ?? '-'}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};
```

---

## Testing

### Manual Testing with STOMP CLI

```bash
# Install stomp-client CLI
npm install -g stomp-client

# Connect and subscribe
stomp-client -H "Authorization:Bearer YOUR_JWT_TOKEN" \
  -s /topic/quotes/RELIANCE \
  ws://localhost:8080/ws
```

### Integration Test with Spring WebSocket Test

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class MarketDataWebSocketIT {

  @LocalServerPort
  private int port;

  private StompSession stompSession;

  @BeforeEach
  void setup() throws Exception {
    WebSocketStompClient stompClient = new WebSocketStompClient(
      new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient())))
    );
    stompClient.setMessageConverter(new MappingJackson2MessageConverter());

    StompHeaders connectHeaders = new StompHeaders();
    connectHeaders.add("Authorization", "Bearer " + getValidJWT());

    stompSession = stompClient
      .connectAsync("ws://localhost:" + port + "/ws", new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {})
      .get(5, TimeUnit.SECONDS);
  }

  @Test
  void shouldReceiveQuoteUpdates() throws Exception {
    BlockingQueue<QuoteDTO> quotes = new LinkedBlockingQueue<>();

    stompSession.subscribe(
      "/topic/quotes/RELIANCE",
      new StompFrameHandler() {
        @Override
        public Type getPayloadType(StompHeaders headers) {
          return QuoteDTO.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
          quotes.add((QuoteDTO) payload);
        }
      }
    );

    // Wait for at least one quote
    QuoteDTO quote = quotes.poll(5, TimeUnit.SECONDS);
    assertNotNull(quote);
    assertEquals("RELIANCE", quote.symbol());
    assertTrue(quote.lastPrice().compareTo(BigDecimal.ZERO) > 0);
  }
}

```

---

## Appendix: Full STOMP Conversation Example

```text
[Client → Server] CONNECT
Authorization:Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
accept-version:1.2
heart-beat:10000,10000

^@

[Server → Client] CONNECTED
version:1.2
heart-beat:10000,10000
session:abc123xyz

^@

[Client → Server] SUBSCRIBE
id:sub-1
destination:/topic/quotes/RELIANCE
ack:auto

^@

[Server → Client] MESSAGE (every 500-1000ms)
destination:/topic/quotes/RELIANCE
message-id:msg-001
subscription:sub-1
content-type:application/json

{"symbol":"RELIANCE","lastPrice":2485.30,"open":2475.00,"change":10.30,"changePercent":0.4162,"volume":1523400,"timestamp":"2025-11-13T10:32:15.123Z"}
^@

[Client → Server] UNSUBSCRIBE
id:sub-1

^@

[Client → Server] DISCONNECT
receipt:disconnect-1

^@

[Server → Client] RECEIPT
receipt-id:disconnect-1

^@
```

---

**Next Steps**: Integrate with `quickstart.md` for end-to-end testing instructions.
