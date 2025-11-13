import dayjs from 'dayjs';
import { IExchange } from 'app/shared/model/exchange.model';
import { AssetClass } from 'app/shared/model/enumerations/asset-class.model';
import { Currency } from 'app/shared/model/enumerations/currency.model';

export interface IBroker {
  id?: number;
  code?: string;
  name?: string;
  status?: string;
  createdDate?: dayjs.Dayjs | null;
  exchange?: IExchange | null;
}

export const defaultValue: Readonly<IBroker> = {};

export interface IBrokerInstrument {
  symbol?: string;
  name?: string;
  exchangeCode?: string;
  assetClass?: AssetClass;
  tickSize?: number;
  lotSize?: number;
  currency?: Currency;
}

export interface IBrokerBaseline {
  id?: number;
  code?: string;
  name?: string;
  status?: string;
  exchangeCode?: string | null;
  exchangeName?: string | null;
  exchangeTimezone?: string | null;
  brokerAdminLogin?: string | null;
  exchangeMemberships?: string[];
  instrumentCatalog?: IBrokerInstrument[];
  instrumentCount?: number;
}
