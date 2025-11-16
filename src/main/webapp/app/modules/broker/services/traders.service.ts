import axios from 'axios';

export interface TraderSummary {
  traderId: string;
  name: string;
  login: string;
  status: 'active' | 'disabled';
  cash: number | string;
  currentPnl: number | string;
}

export interface TraderPage {
  content: TraderSummary[];
  page: number;
  size: number;
  totalElements: number;
}

export interface LedgerEntry {
  id: string;
  type: 'JOURNAL_CREDIT' | 'JOURNAL_DEBIT' | 'OTHER';
  amount: number | string;
  reason: string;
  createdAt: string;
  createdByUserId: string;
}

export interface TraderDetails {
  summary: TraderSummary;
  recentLedger: LedgerEntry[];
}

export const getTraders = (page = 0, size = 20) =>
  axios.get<TraderPage>('/api/broker/traders', { params: { page, size } }).then(r => r.data);

export const getTraderDetails = (traderId: string) => axios.get<TraderDetails>(`/api/broker/traders/${traderId}`).then(r => r.data);

export type JournalDirection = 'credit' | 'debit';

export interface JournalRequest {
  direction: JournalDirection;
  amount: number;
  reason: string;
}

export interface JournalResult {
  ledgerEntry: LedgerEntry;
  account: { tradingAccountId: string; cash: number };
}

export const postJournal = (tradingAccountId: string, payload: JournalRequest) =>
  axios
    .post<JournalResult>(`/api/broker/traders/${tradingAccountId}/journal`, payload, {
      headers: { 'Idempotency-Key': `ui-${Date.now()}-${Math.random().toString(36).slice(2)}` },
    })
    .then(r => r.data);
