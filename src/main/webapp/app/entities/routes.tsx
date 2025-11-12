import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Exchange from './exchange';
import Broker from './broker';
import ExchangeIntegration from './exchange-integration';
import MarketHoliday from './market-holiday';
import BrokerDesk from './broker-desk';
import ExchangeOperator from './exchange-operator';
import TraderProfile from './trader-profile';
import Instrument from './instrument';
import Contract from './contract';
import DailySettlementPrice from './daily-settlement-price';
import TradingAccount from './trading-account';
import Order from './order';
import Execution from './execution';
import Position from './position';
import Lot from './lot';
import LedgerEntry from './ledger-entry';
import MarginRule from './margin-rule';
import RiskAlert from './risk-alert';
import CorporateAction from './corporate-action';
import SettlementBatch from './settlement-batch';
/* jhipster-needle-add-route-import - JHipster will add routes here */

export default () => {
  return (
    <div>
      <ErrorBoundaryRoutes>
        {/* prettier-ignore */}
        <Route path="exchange/*" element={<Exchange />} />
        <Route path="broker/*" element={<Broker />} />
        <Route path="exchange-integration/*" element={<ExchangeIntegration />} />
        <Route path="market-holiday/*" element={<MarketHoliday />} />
        <Route path="broker-desk/*" element={<BrokerDesk />} />
        <Route path="exchange-operator/*" element={<ExchangeOperator />} />
        <Route path="trader-profile/*" element={<TraderProfile />} />
        <Route path="instrument/*" element={<Instrument />} />
        <Route path="contract/*" element={<Contract />} />
        <Route path="daily-settlement-price/*" element={<DailySettlementPrice />} />
        <Route path="trading-account/*" element={<TradingAccount />} />
        <Route path="order/*" element={<Order />} />
        <Route path="execution/*" element={<Execution />} />
        <Route path="position/*" element={<Position />} />
        <Route path="lot/*" element={<Lot />} />
        <Route path="ledger-entry/*" element={<LedgerEntry />} />
        <Route path="margin-rule/*" element={<MarginRule />} />
        <Route path="risk-alert/*" element={<RiskAlert />} />
        <Route path="corporate-action/*" element={<CorporateAction />} />
        <Route path="settlement-batch/*" element={<SettlementBatch />} />
        {/* jhipster-needle-add-route-path - JHipster will add routes here */}
      </ErrorBoundaryRoutes>
    </div>
  );
};
