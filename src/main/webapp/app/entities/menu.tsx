import React from 'react';
import { Translate } from 'react-jhipster';

import MenuItem from 'app/shared/layout/menus/menu-item';

const EntitiesMenu = () => {
  return (
    <>
      {/* prettier-ignore */}
      <MenuItem icon="asterisk" to="/exchange">
        <Translate contentKey="global.menu.entities.exchange" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/broker">
        <Translate contentKey="global.menu.entities.broker" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/exchange-integration">
        <Translate contentKey="global.menu.entities.exchangeIntegration" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/market-holiday">
        <Translate contentKey="global.menu.entities.marketHoliday" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/broker-desk">
        <Translate contentKey="global.menu.entities.brokerDesk" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/exchange-operator">
        <Translate contentKey="global.menu.entities.exchangeOperator" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/trader-profile">
        <Translate contentKey="global.menu.entities.traderProfile" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/instrument">
        <Translate contentKey="global.menu.entities.instrument" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/contract">
        <Translate contentKey="global.menu.entities.contract" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/daily-settlement-price">
        <Translate contentKey="global.menu.entities.dailySettlementPrice" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/trading-account">
        <Translate contentKey="global.menu.entities.tradingAccount" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/order">
        <Translate contentKey="global.menu.entities.order" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/execution">
        <Translate contentKey="global.menu.entities.execution" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/position">
        <Translate contentKey="global.menu.entities.position" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/lot">
        <Translate contentKey="global.menu.entities.lot" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/ledger-entry">
        <Translate contentKey="global.menu.entities.ledgerEntry" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/margin-rule">
        <Translate contentKey="global.menu.entities.marginRule" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/risk-alert">
        <Translate contentKey="global.menu.entities.riskAlert" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/corporate-action">
        <Translate contentKey="global.menu.entities.corporateAction" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/settlement-batch">
        <Translate contentKey="global.menu.entities.settlementBatch" />
      </MenuItem>
      {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
    </>
  );
};

export default EntitiesMenu;
