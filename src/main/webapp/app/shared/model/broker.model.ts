import dayjs from 'dayjs';
import { IExchange } from 'app/shared/model/exchange.model';

export interface IBroker {
  id?: number;
  code?: string;
  name?: string;
  status?: string;
  createdDate?: dayjs.Dayjs | null;
  exchange?: IExchange | null;
}

export const defaultValue: Readonly<IBroker> = {};
