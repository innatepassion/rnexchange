import dayjs from 'dayjs';
import { ITradingAccount } from 'app/shared/model/trading-account.model';
import { Currency } from 'app/shared/model/enumerations/currency.model';

export interface ILedgerEntry {
  id?: number;
  ts?: dayjs.Dayjs;
  type?: string;
  amount?: number;
  ccy?: keyof typeof Currency;
  balanceAfter?: number | null;
  reference?: string | null;
  remarks?: string | null;
  tradingAccount?: ITradingAccount | null;
}

export const defaultValue: Readonly<ILedgerEntry> = {};
