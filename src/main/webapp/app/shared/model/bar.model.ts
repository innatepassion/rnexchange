export interface IBar {
  symbol: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
  timestamp: string;
  exchangeCode?: string;
}

export type NewBar = Omit<IBar, 'symbol'>;
