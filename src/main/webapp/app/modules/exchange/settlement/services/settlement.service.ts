import axios from 'axios';

export interface SettlementBatchDTO {
  id?: number;
  refDate: string;
  kind: string;
  status: string;
  remarks?: string;
  accountsProcessed?: number;
  positionsProcessed?: number;
  netPnl?: number;
}

export const runEod = (date: string): Promise<SettlementBatchDTO> => {
  return axios.post<SettlementBatchDTO>('/api/settlements/eod', null, { params: { date } }).then(r => r.data);
};

export const getSettlementBatches = (from: string, to: string): Promise<SettlementBatchDTO[]> => {
  return axios.get<SettlementBatchDTO[]>('/api/settlements', { params: { from, to } }).then(r => r.data);
};
