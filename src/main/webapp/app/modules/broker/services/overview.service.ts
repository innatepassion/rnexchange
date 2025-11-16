import axios from 'axios';

export interface BrokerOverview {
  activeTraderCount: number;
  totalCash: string; // BigDecimal serialized
  totalEquityExposure: string; // BigDecimal serialized
  generatedAt: string;
}

export const getBrokerOverview = (brokerId: number) =>
  axios.get<BrokerOverview>('/api/broker/overview', { params: { brokerId } }).then(r => r.data);
