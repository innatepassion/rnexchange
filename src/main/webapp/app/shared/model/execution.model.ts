import dayjs from 'dayjs';
import { IOrder } from 'app/shared/model/order.model';

export interface IExecution {
  id?: number;
  execTs?: dayjs.Dayjs;
  px?: number;
  qty?: number;
  liquidity?: string | null;
  fee?: number | null;
  order?: IOrder | null;
}

export const defaultValue: Readonly<IExecution> = {};
