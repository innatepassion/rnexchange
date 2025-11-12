import dayjs from 'dayjs';
import { IInstrument } from 'app/shared/model/instrument.model';

export interface IDailySettlementPrice {
  id?: number;
  refDate?: dayjs.Dayjs;
  instrumentSymbol?: string;
  settlePrice?: number;
  instrument?: IInstrument | null;
}

export const defaultValue: Readonly<IDailySettlementPrice> = {};
