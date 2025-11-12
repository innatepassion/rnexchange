import { IUser } from 'app/shared/model/user.model';
import { KycStatus } from 'app/shared/model/enumerations/kyc-status.model';
import { AccountStatus } from 'app/shared/model/enumerations/account-status.model';

export interface ITraderProfile {
  id?: number;
  displayName?: string;
  email?: string;
  mobile?: string | null;
  kycStatus?: keyof typeof KycStatus;
  status?: keyof typeof AccountStatus;
  user?: IUser | null;
}

export const defaultValue: Readonly<ITraderProfile> = {};
