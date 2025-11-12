import dayjs from 'dayjs';
import { IInstrument } from 'app/shared/model/instrument.model';
import { CorporateActionType } from 'app/shared/model/enumerations/corporate-action-type.model';

export interface ICorporateAction {
  id?: number;
  type?: keyof typeof CorporateActionType;
  instrumentSymbol?: string;
  exDate?: dayjs.Dayjs;
  payDate?: dayjs.Dayjs | null;
  ratio?: number | null;
  cashAmount?: number | null;
  instrument?: IInstrument | null;
}

export const defaultValue: Readonly<ICorporateAction> = {};
