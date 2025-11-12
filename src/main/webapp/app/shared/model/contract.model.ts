import dayjs from 'dayjs';
import { IInstrument } from 'app/shared/model/instrument.model';
import { ContractType } from 'app/shared/model/enumerations/contract-type.model';
import { OptionType } from 'app/shared/model/enumerations/option-type.model';

export interface IContract {
  id?: number;
  instrumentSymbol?: string;
  contractType?: keyof typeof ContractType;
  expiry?: dayjs.Dayjs;
  strike?: number | null;
  optionType?: keyof typeof OptionType | null;
  segment?: string;
  instrument?: IInstrument | null;
}

export const defaultValue: Readonly<IContract> = {};
