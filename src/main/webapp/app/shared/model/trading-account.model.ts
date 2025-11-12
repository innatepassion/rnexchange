import { IBroker } from 'app/shared/model/broker.model';
import { ITraderProfile } from 'app/shared/model/trader-profile.model';
import { AccountType } from 'app/shared/model/enumerations/account-type.model';
import { Currency } from 'app/shared/model/enumerations/currency.model';
import { AccountStatus } from 'app/shared/model/enumerations/account-status.model';

export interface ITradingAccount {
  id?: number;
  type?: keyof typeof AccountType;
  baseCcy?: keyof typeof Currency;
  balance?: number;
  status?: keyof typeof AccountStatus;
  broker?: IBroker | null;
  trader?: ITraderProfile | null;
}

export const defaultValue: Readonly<ITradingAccount> = {};
