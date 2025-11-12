import dayjs from 'dayjs';
import { IExchange } from 'app/shared/model/exchange.model';
import { SettlementKind } from 'app/shared/model/enumerations/settlement-kind.model';
import { SettlementStatus } from 'app/shared/model/enumerations/settlement-status.model';

export interface ISettlementBatch {
  id?: number;
  refDate?: dayjs.Dayjs;
  kind?: keyof typeof SettlementKind;
  status?: keyof typeof SettlementStatus;
  remarks?: string | null;
  exchange?: IExchange | null;
}

export const defaultValue: Readonly<ISettlementBatch> = {};
