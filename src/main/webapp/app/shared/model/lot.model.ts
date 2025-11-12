import dayjs from 'dayjs';
import { IPosition } from 'app/shared/model/position.model';

export interface ILot {
  id?: number;
  openTs?: dayjs.Dayjs;
  openPx?: number;
  qtyOpen?: number;
  qtyClosed?: number;
  method?: string | null;
  position?: IPosition | null;
}

export const defaultValue: Readonly<ILot> = {};
