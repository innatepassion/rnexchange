import React from 'react';
import { DropdownItem } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faWallet, faChartPie, faClipboardList } from '@fortawesome/free-solid-svg-icons';
import { NavLink as Link } from 'react-router-dom';
import { NavDropdown } from './menu-components';

export const TraderMenu = () => (
  <NavDropdown icon={faWallet} name="Trading" id="trader-menu" data-cy="traderMenu">
    <DropdownItem tag={Link} to="/trader?tab=portfolio">
      <FontAwesomeIcon icon={faChartPie} className="me-2" />
      Portfolio & Cash
    </DropdownItem>
    <DropdownItem tag={Link} to="/trader?tab=orders">
      <FontAwesomeIcon icon={faClipboardList} className="me-2" />
      Orders & Trades
    </DropdownItem>
  </NavDropdown>
);

export default TraderMenu;
