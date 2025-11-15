import axios from 'axios';
import { IOrder } from 'app/shared/model/order.model';
import { IExecution } from 'app/shared/model/execution.model';
import { IPosition } from 'app/shared/model/position.model';
import { ILedgerEntry } from 'app/shared/model/ledger-entry.model';

export interface NewOrderRequest {
  tradingAccountId: number;
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
}

export interface CashBalanceView {
  balance: number;
  updatedAt: string;
}

// Place a new order
export const placeOrder = (request: NewOrderRequest) => axios.post<OrderResponse>('/api/orders', request);

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
