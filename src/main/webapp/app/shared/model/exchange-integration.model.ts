import dayjs from 'dayjs';
import { IExchange } from 'app/shared/model/exchange.model';
import { IntegrationStatus } from 'app/shared/model/enumerations/integration-status.model';

export interface IExchangeIntegration {
  id?: number;
  provider?: string;
  apiKey?: string | null;
  apiSecret?: string | null;
  status?: keyof typeof IntegrationStatus;
  lastHeartbeat?: dayjs.Dayjs | null;
  exchange?: IExchange | null;
}

export const defaultValue: Readonly<IExchangeIntegration> = {};
