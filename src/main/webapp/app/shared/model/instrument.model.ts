import { IExchange } from 'app/shared/model/exchange.model';
import { AssetClass } from 'app/shared/model/enumerations/asset-class.model';
import { Currency } from 'app/shared/model/enumerations/currency.model';

export interface IInstrument {
  id?: number;
  symbol?: string;
  name?: string | null;
  assetClass?: keyof typeof AssetClass;
  exchangeCode?: string;
  tickSize?: number;
  lotSize?: number;
  currency?: keyof typeof Currency;
  status?: string;
  exchange?: IExchange | null;
}

export const defaultValue: Readonly<IInstrument> = {};
