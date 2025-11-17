import axios from 'axios';

export interface StatementSummary {
  id?: number;
  refDate: string;
  tradingAccountId: number;
  tradingAccountLabel?: string;
  openingBalance: number;
  netCashFlows?: number;
  eodMtmPnl: number;
  closingBalance: number;
  htmlUrl: string;
}

export const getStatements = (from?: string, to?: string) => {
  const params: Record<string, string> = {};
  if (from) params.from = from;
  if (to) params.to = to;
  return axios.get<StatementSummary[]>('/api/statements', { params }).then(r => r.data);
};
