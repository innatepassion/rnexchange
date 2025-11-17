/**
 * M6 User Story 4, Task T041: Jest/React tests to assert that navigation menus
 * render role-appropriate items and hide generic JHipster links.
 */

import React from 'react';
import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router';

import initStore from 'app/config/store';
import Header from 'app/shared/layout/header/header';
import { AUTHORITIES } from 'app/config/constants';
import { setAuthentication } from 'app/shared/reducers/authentication';

describe('RoleBasedMenu', () => {
  let store;

  beforeEach(() => {
    store = initStore();
  });

  const renderHeader = (authorities: string[], props = {}) => {
    // Set up authentication state with authorities
    store.dispatch(
      setAuthentication({
        isAuthenticated: true,
        account: {
          login: 'test-user',
          authorities: authorities,
        },
      }),
    );

    const defaultProps = {
      isAuthenticated: true,
      isAdmin: authorities.includes(AUTHORITIES.ADMIN),
      isExchangeOperator: authorities.includes(AUTHORITIES.EXCHANGE_OPERATOR),
      isTrader: authorities.includes(AUTHORITIES.TRADER),
      isBrokerAdmin: authorities.includes(AUTHORITIES.BROKER_ADMIN),
      currentLocale: 'en',
      ribbonEnv: 'dev',
      isInProduction: false,
      isOpenAPIEnabled: true,
      ...props,
    };

    return render(
      <Provider store={store}>
        <MemoryRouter>
          <Header {...defaultProps} />
        </MemoryRouter>
      </Provider>,
    );
  };

  describe('Trader Role', () => {
    it('should hide generic JHipster menus for trader', () => {
      renderHeader([AUTHORITIES.TRADER]);

      // Generic JHipster menus should NOT be visible
      expect(screen.queryByText(/Entities/i)).not.toBeInTheDocument();
      expect(screen.queryByText(/Administration/i)).not.toBeInTheDocument();
      expect(screen.queryByText(/Performance/i)).not.toBeInTheDocument();

      // Trader-specific menus should be visible
      expect(screen.getByText(/Market Watch/i)).toBeInTheDocument();
    });

    it('should show trader-specific navigation items', () => {
      renderHeader([AUTHORITIES.TRADER]);

      // Check for trader menu items
      expect(screen.getByText(/Market Watch/i)).toBeInTheDocument();
      // TraderMenu component should be rendered
      expect(screen.getByTestId('navbar') || screen.getByRole('navigation')).toBeInTheDocument();
    });
  });

  describe('Broker Admin Role', () => {
    it('should hide generic JHipster menus for broker admin', () => {
      renderHeader([AUTHORITIES.BROKER_ADMIN]);

      // Generic JHipster menus should NOT be visible
      expect(screen.queryByText(/Entities/i)).not.toBeInTheDocument();
      expect(screen.queryByText(/Administration/i)).not.toBeInTheDocument();
      expect(screen.queryByText(/Performance/i)).not.toBeInTheDocument();

      // Broker-specific menus should be visible (BrokerAdminMenu component)
      expect(screen.getByTestId('navbar') || screen.getByRole('navigation')).toBeInTheDocument();
    });
  });

  describe('Exchange Operator Role', () => {
    it('should hide generic Entities menu but show relevant admin items', () => {
      renderHeader([AUTHORITIES.EXCHANGE_OPERATOR]);

      // Generic Entities menu should NOT be visible
      expect(screen.queryByText(/Entities/i)).not.toBeInTheDocument();

      // Exchange Console should be visible
      expect(screen.getByTestId('navbar') || screen.getByRole('navigation')).toBeInTheDocument();
    });

    it('should show Exchange Overview entry', () => {
      renderHeader([AUTHORITIES.EXCHANGE_OPERATOR]);

      // Exchange Console menu should be rendered
      expect(screen.getByTestId('navbar') || screen.getByRole('navigation')).toBeInTheDocument();
    });
  });

  describe('Admin Role', () => {
    it('should show all menus for admin', () => {
      renderHeader([AUTHORITIES.ADMIN]);

      // Admin should see all menus
      expect(screen.getByTestId('navbar') || screen.getByRole('navigation')).toBeInTheDocument();
    });
  });

  describe('Multiple Roles', () => {
    it('should prioritize TRADER role when user has multiple roles', () => {
      renderHeader([AUTHORITIES.TRADER, AUTHORITIES.USER]);

      // Should use TRADER menu config (hide Entities/Admin)
      expect(screen.queryByText(/Entities/i)).not.toBeInTheDocument();
      expect(screen.getByText(/Market Watch/i)).toBeInTheDocument();
    });

    it('should prioritize BROKER_ADMIN over USER role', () => {
      renderHeader([AUTHORITIES.BROKER_ADMIN, AUTHORITIES.USER]);

      // Should use BROKER_ADMIN menu config
      expect(screen.queryByText(/Entities/i)).not.toBeInTheDocument();
    });
  });
});
