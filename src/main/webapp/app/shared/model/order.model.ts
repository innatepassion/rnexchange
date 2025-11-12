import dayjs from 'dayjs';
import { ITradingAccount } from 'app/shared/model/trading-account.model';
import { IInstrument } from 'app/shared/model/instrument.model';
import { OrderSide } from 'app/shared/model/enumerations/order-side.model';
import { OrderType } from 'app/shared/model/enumerations/order-type.model';
import { Tif } from 'app/shared/model/enumerations/tif.model';
import { OrderStatus } from 'app/shared/model/enumerations/order-status.model';

export interface IOrder {
  id?: number;
  side?: keyof typeof OrderSide;
  type?: keyof typeof OrderType;
  qty?: number;
  limitPx?: number | null;
  stopPx?: number | null;
  tif?: keyof typeof Tif;
  status?: keyof typeof OrderStatus;
  venue?: string;
  createdAt?: dayjs.Dayjs | null;
  updatedAt?: dayjs.Dayjs | null;
  tradingAccount?: ITradingAccount | null;
  instrument?: IInstrument | null;
}

export const defaultValue: Readonly<IOrder> = {};
