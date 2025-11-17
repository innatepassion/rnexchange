import axios from 'axios';
import { IOrder } from 'app/shared/model/order.model';
import { IExecution } from 'app/shared/model/execution.model';
import { IPosition } from 'app/shared/model/position.model';
import { ILedgerEntry } from 'app/shared/model/ledger-entry.model';
import { ITradingAccount } from 'app/shared/model/trading-account.model';

export interface NewOrderRequest {
  tradingAccountId: number; // kept for future use, backend resolves account from logged-in trader
  instrumentId: number;
  side: 'BUY' | 'SELL';
  type: 'MARKET' | 'LIMIT';
  quantity: number;
  limitPrice?: number;
}

export interface OrderResponse {
  id?: number;
  side?: string;
  type?: string;
  qty?: number;
  limitPx?: number | null;
  status?: string;
  execution?: IExecution | null;
  createdAt?: string;
  updatedAt?: string;
  message?: string;
}

export interface PositionView {
  id?: number;
  qty?: number;
  avgCost?: number;
  lastPx?: number;
  mtm?: number;
  unrealizedPnl?: number;
  realizedPnl?: number;
  instrument?: {
    id?: number;
    symbol?: string;
    exchange?: string;
  };
}

export interface LedgerEntryView {
  id?: number;
  type?: string;
  amount?: number;
  fee?: number;
  description?: string;
  createdAt?: string;
  balanceAfter?: number; // T017 [US1]: Running balance after this transaction
  reference?: string; // T017 [US1]: Order reference (e.g., "ORD-123")
}

export interface CashBalanceView {
  balance: number;
  updatedAt: string;
}

export type TradingAccountView = ITradingAccount;

// Place a new trading order (maps to POST /api/orders/trading)
export const placeOrder = (request: NewOrderRequest) => {
  const payload = {
    instrument: { id: request.instrumentId },
    side: request.side,
    type: request.type,
    qty: request.quantity,
    // For MARKET orders, backend ignores limitPx; for LIMIT we forward the price when provided
    limitPx: request.type === 'LIMIT' && request.limitPrice !== undefined ? request.limitPrice : undefined,
    // Required by OrderDTO validation, but the trading endpoint will override status/venue as needed
    tif: 'DAY',
    status: 'NEW',
    venue: 'NSE',
  };

  return axios.post<OrderResponse>('/api/orders/trading', payload);
};

// Get positions for a trading account
export const getPositions = (accountId: number | string, page?: number, size?: number) => {
  const params = new URLSearchParams();
  if (page !== undefined) params.append('page', page.toString());
  if (size !== undefined) params.append('size', size.toString());
  const query = params.toString();
  return axios.get<PositionView[]>(`/api/trading-accounts/${accountId}/positions${query ? '?' + query : ''}`);
};

// Get recent orders for a trading account
export const getOrders = (accountId: number | string, page?: number, size?: number) => {
  const params = new URLSearchParams();
  if (page !== undefined) params.append('page', page.toString());
  if (size !== undefined) params.append('size', size.toString());
  const query = params.toString();
  return axios.get<IOrder[]>(`/api/trading-accounts/${accountId}/orders${query ? '?' + query : ''}`);
};

// Get executions for a trading account
export const getExecutions = (accountId: number | string, page?: number, size?: number) => {
  const params = new URLSearchParams();
  if (page !== undefined) params.append('page', page.toString());
  if (size !== undefined) params.append('size', size.toString());
  const query = params.toString();
  return axios.get<IExecution[]>(`/api/trading-accounts/${accountId}/executions${query ? '?' + query : ''}`);
};

// Get ledger entries for a trading account
export const getLedgerEntries = (accountId: number | string, page?: number, size?: number) => {
  const params = new URLSearchParams();
  if (page !== undefined) params.append('page', page.toString());
  if (size !== undefined) params.append('size', size.toString());
  const query = params.toString();
  return axios.get<LedgerEntryView[]>(`/api/trading-accounts/${accountId}/ledger-entries${query ? '?' + query : ''}`);
};

// Get current cash balance
export const getCashBalance = (accountId: number | string) => axios.get<CashBalanceView>(`/api/trading-accounts/${accountId}/balance`);

// Get the current trader's default trading account (resolved on the backend from login)
export const getCurrentTradingAccount = () => axios.get<TradingAccountView>('/api/trading-accounts/current');

// Get instrument by symbol
export const getInstrumentBySymbol = (symbol: string) => {
  const params = new URLSearchParams();
  params.append('symbol.equals', symbol);
  return axios.get<any>(`/api/instruments?${params.toString()}`);
};
