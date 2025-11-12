import exchange from 'app/entities/exchange/exchange.reducer';
import broker from 'app/entities/broker/broker.reducer';
import exchangeIntegration from 'app/entities/exchange-integration/exchange-integration.reducer';
import marketHoliday from 'app/entities/market-holiday/market-holiday.reducer';
import brokerDesk from 'app/entities/broker-desk/broker-desk.reducer';
import exchangeOperator from 'app/entities/exchange-operator/exchange-operator.reducer';
import traderProfile from 'app/entities/trader-profile/trader-profile.reducer';
import instrument from 'app/entities/instrument/instrument.reducer';
import contract from 'app/entities/contract/contract.reducer';
import dailySettlementPrice from 'app/entities/daily-settlement-price/daily-settlement-price.reducer';
import tradingAccount from 'app/entities/trading-account/trading-account.reducer';
import order from 'app/entities/order/order.reducer';
import execution from 'app/entities/execution/execution.reducer';
import position from 'app/entities/position/position.reducer';
import lot from 'app/entities/lot/lot.reducer';
import ledgerEntry from 'app/entities/ledger-entry/ledger-entry.reducer';
import marginRule from 'app/entities/margin-rule/margin-rule.reducer';
import riskAlert from 'app/entities/risk-alert/risk-alert.reducer';
import corporateAction from 'app/entities/corporate-action/corporate-action.reducer';
import settlementBatch from 'app/entities/settlement-batch/settlement-batch.reducer';
/* jhipster-needle-add-reducer-import - JHipster will add reducer here */

const entitiesReducers = {
  exchange,
  broker,
  exchangeIntegration,
  marketHoliday,
  brokerDesk,
  exchangeOperator,
  traderProfile,
  instrument,
  contract,
  dailySettlementPrice,
  tradingAccount,
  order,
  execution,
  position,
  lot,
  ledgerEntry,
  marginRule,
  riskAlert,
  corporateAction,
  settlementBatch,
  /* jhipster-needle-add-reducer-combine - JHipster will add reducer here */
};

export default entitiesReducers;
