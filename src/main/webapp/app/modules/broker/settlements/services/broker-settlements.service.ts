import axios from 'axios';

export interface BrokerSettlementSummary {
  refDate: string;
  brokerId: number;
  brokerName: string;
  totalClientCount: number;
  totalOpeningBalance: number;
  totalClosingBalance: number;
  totalEodMtmPnl: number;
  summaryUrl: string;
}

export interface StatementSummary {
  id: number;
  refDate: string;
  tradingAccountId: number;
  tradingAccountLabel: string;
  openingBalance: number;
  netCashFlows: number;
  eodMtmPnl: number;
  closingBalance: number;
  htmlUrl: string;
}

export const getBrokerSettlements = (from?: string, to?: string) => {
  const params: Record<string, string> = {};
  if (from) params.from = from;
  if (to) params.to = to;
  return axios.get<BrokerSettlementSummary[]>('/api/broker/settlements', { params }).then(r => r.data);
};

export const getClientStatements = (date: string) => {
  return axios.get<StatementSummary[]>('/api/broker/settlements/client-statements', { params: { date } }).then(r => r.data);
};
