import React from 'react';
import { DropdownItem } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faBriefcase, faChartBar } from '@fortawesome/free-solid-svg-icons';
import { NavLink as Link } from 'react-router-dom';
import { NavDropdown } from './menu-components';

export const BrokerAdminMenu = () => (
  <NavDropdown icon={faBriefcase} name="Back Office" id="broker-admin-menu" data-cy="brokerAdminMenu">
    <DropdownItem tag={Link} to="/broker/dashboard">
      <FontAwesomeIcon icon={faChartBar} className="me-2" />
      Broker Dashboard
    </DropdownItem>
  </NavDropdown>
);

export default BrokerAdminMenu;
