import dayjs from 'dayjs';
import { IExchange } from 'app/shared/model/exchange.model';

export interface IMarketHoliday {
  id?: number;
  tradeDate?: dayjs.Dayjs;
  reason?: string | null;
  isHoliday?: boolean;
  exchange?: IExchange | null;
}

export const defaultValue: Readonly<IMarketHoliday> = {
  isHoliday: false,
};
