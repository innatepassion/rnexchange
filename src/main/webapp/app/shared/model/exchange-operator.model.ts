import { IUser } from 'app/shared/model/user.model';
import { IExchange } from 'app/shared/model/exchange.model';

export interface IExchangeOperator {
  id?: number;
  name?: string;
  user?: IUser | null;
  exchange?: IExchange | null;
}

export const defaultValue: Readonly<IExchangeOperator> = {};
