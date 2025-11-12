import { IExchange } from 'app/shared/model/exchange.model';

export interface IMarginRule {
  id?: number;
  scope?: string;
  initialPct?: number | null;
  maintPct?: number | null;
  spanJson?: string | null;
  exchange?: IExchange | null;
}

export const defaultValue: Readonly<IMarginRule> = {};
