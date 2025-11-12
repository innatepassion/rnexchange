import { ExchangeStatus } from 'app/shared/model/enumerations/exchange-status.model';

export interface IExchange {
  id?: number;
  code?: string;
  name?: string;
  timezone?: string;
  status?: keyof typeof ExchangeStatus;
}

export const defaultValue: Readonly<IExchange> = {};
