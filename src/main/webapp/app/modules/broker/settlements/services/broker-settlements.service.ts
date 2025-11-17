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

export const getBrokerSettlements = (from?: string, to?: string) => {
  const params: Record<string, string> = {};
  if (from) params.from = from;
  if (to) params.to = to;
  return axios.get<BrokerSettlementSummary[]>('/api/broker/settlements', { params }).then(r => r.data);
};
