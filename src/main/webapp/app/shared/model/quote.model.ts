export interface IQuote {
  symbol: string;
  lastPrice: number;
  open: number;
  change: number;
  changePercent: number;
  volume: number;
  timestamp: string;
  marketStatus?: 'OPEN' | 'HOLIDAY' | 'PAUSED';
  exchangeCode?: string;
}

export type NewQuote = Omit<IQuote, 'symbol'>;
