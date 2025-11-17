import './header.scss';

import React, { useState } from 'react';
import { Storage, Translate } from 'react-jhipster';
import { Collapse, Nav, NavItem, NavLink, Navbar, NavbarToggler } from 'reactstrap';
import LoadingBar from 'react-redux-loading-bar';
import { NavLink as Link } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faChartLine } from '@fortawesome/free-solid-svg-icons';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { setLocale } from 'app/shared/reducers/locale';
import { AccountMenu, AdminMenu, EntitiesMenu, LocaleMenu, ExchangeConsoleMenu, TraderMenu, BrokerAdminMenu } from '../menus';
import { resolveRoleMenuConfig } from '../menus';
import { Brand, Home } from './header-components';

export interface IHeaderProps {
  isAuthenticated: boolean;
  isAdmin: boolean;
  isExchangeOperator: boolean;
  isTrader: boolean;
  isBrokerAdmin: boolean;
  ribbonEnv: string;
  isInProduction: boolean;
  isOpenAPIEnabled: boolean;
  currentLocale: string;
}

const Header = (props: IHeaderProps) => {
  const [menuOpen, setMenuOpen] = useState(false);

  const dispatch = useAppDispatch();
  // M6 Phase 2: Get account authorities for role-based menu configuration
  const account = useAppSelector(state => state.authentication.account);
  const authorities = account?.authorities || [];
  const menuConfig = resolveRoleMenuConfig(authorities);

  const handleLocaleChange = event => {
    const langKey = event.target.value;
    Storage.session.set('locale', langKey);
    dispatch(setLocale(langKey));
  };

  const renderDevRibbon = () =>
    props.isInProduction === false ? (
      <div className="ribbon dev">
        <a href="">
          <Translate contentKey={`global.ribbon.${props.ribbonEnv}`} />
        </a>
      </div>
    ) : null;

  const toggleMenu = () => setMenuOpen(!menuOpen);

  /* jhipster-needle-add-element-to-menu - JHipster will add new menu items here */

  return (
    <div id="app-header">
      {renderDevRibbon()}
      <LoadingBar className="loading-bar" />
      <Navbar data-cy="navbar" dark expand="md" fixed="top" className="jh-navbar">
        <NavbarToggler aria-label="Menu" onClick={toggleMenu} />
        <Brand />
        <Collapse isOpen={menuOpen} navbar>
          <Nav id="header-tabs" className="ms-auto" navbar>
            <Home />
            {/* M6 Phase 2: Use centralized menu configuration */}
            {props.isAuthenticated && menuConfig.showEntities && <EntitiesMenu />}
            {props.isAuthenticated && menuConfig.showAdmin && (
              <AdminMenu showOpenAPI={props.isOpenAPIEnabled} showDatabase={!props.isInProduction} />
            )}
            {props.isAuthenticated && menuConfig.showExchangeConsole && <ExchangeConsoleMenu />}
            {props.isAuthenticated && menuConfig.showTrader && (
              <>
                {menuConfig.showMarketWatch && (
                  <NavItem>
                    <NavLink tag={Link} to="/market-watch" className="d-flex align-items-center">
                      <FontAwesomeIcon icon={faChartLine} className="me-1" />
                      <span>Market Watch</span>
                    </NavLink>
                  </NavItem>
                )}
                <TraderMenu />
              </>
            )}
            {props.isAuthenticated && menuConfig.showBrokerAdmin && <BrokerAdminMenu />}
            <LocaleMenu currentLocale={props.currentLocale} onClick={handleLocaleChange} />
            <AccountMenu isAuthenticated={props.isAuthenticated} />
          </Nav>
        </Collapse>
      </Navbar>
    </div>
  );
};

export default Header;
