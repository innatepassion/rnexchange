import { IUser } from 'app/shared/model/user.model';
import { IBroker } from 'app/shared/model/broker.model';

export interface IBrokerDesk {
  id?: number;
  name?: string;
  user?: IUser | null;
  broker?: IBroker | null;
}

export const defaultValue: Readonly<IBrokerDesk> = {};
