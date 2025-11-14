import React from 'react';
import MenuItem from 'app/shared/layout/menus/menu-item';
import { NavDropdown } from './menu-components';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

export const ExchangeConsoleMenu = () => (
  <NavDropdown icon="tachometer-alt" name="Exchange Console" id="exchange-console-menu" data-cy="exchange-console">
    <MenuItem icon="chart-line" to="/exchange-console">
      Market Data Feed
    </MenuItem>
  </NavDropdown>
);
