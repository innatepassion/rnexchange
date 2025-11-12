import { ITradingAccount } from 'app/shared/model/trading-account.model';
import { IInstrument } from 'app/shared/model/instrument.model';

export interface IPosition {
  id?: number;
  qty?: number;
  avgCost?: number;
  lastPx?: number | null;
  unrealizedPnl?: number | null;
  realizedPnl?: number | null;
  tradingAccount?: ITradingAccount | null;
  instrument?: IInstrument | null;
}

export const defaultValue: Readonly<IPosition> = {};
