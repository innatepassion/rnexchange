import React from 'react';
import { DropdownItem } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faBriefcase, faChartBar, faBook, faList } from '@fortawesome/free-solid-svg-icons';
import { NavLink as Link } from 'react-router-dom';
import { NavDropdown } from './menu-components';

export const BrokerAdminMenu = () => (
  <NavDropdown icon={faBriefcase} name="Back Office" id="broker-admin-menu" data-cy="brokerAdminMenu">
    <DropdownItem tag={Link} to="/broker/dashboard">
      <FontAwesomeIcon icon={faChartBar} className="me-2" />
      Broker Dashboard
    </DropdownItem>
    <DropdownItem tag={Link} to="/broker/journal">
      <FontAwesomeIcon icon={faBook} className="me-2" />
      Funds Journal
    </DropdownItem>
    <DropdownItem tag={Link} to="/broker/journal-entries">
      <FontAwesomeIcon icon={faList} className="me-2" />
      View Journal Entries
    </DropdownItem>
  </NavDropdown>
);

export default BrokerAdminMenu;
