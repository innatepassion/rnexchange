import dayjs from 'dayjs';
import { ITradingAccount } from 'app/shared/model/trading-account.model';
import { ITraderProfile } from 'app/shared/model/trader-profile.model';
import { AlertType } from 'app/shared/model/enumerations/alert-type.model';

export interface IRiskAlert {
  id?: number;
  alertType?: keyof typeof AlertType;
  description?: string | null;
  createdAt?: dayjs.Dayjs | null;
  tradingAccount?: ITradingAccount | null;
  trader?: ITraderProfile | null;
}

export const defaultValue: Readonly<IRiskAlert> = {};
