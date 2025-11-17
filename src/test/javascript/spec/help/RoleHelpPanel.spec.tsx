/**
 * M6 User Story 5, Task T050: Jest/React tests verifying that the help panel component
 * renders the correct role-specific content and links.
 */

import '@testing-library/jest-dom';
import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router';
import { TranslatorContext } from 'react-jhipster';

import initStore from 'app/config/store';
import RoleHelpPanel from 'app/shared/components/RoleHelpPanel';
import { AUTHORITIES } from 'app/config/constants';
import { setAuthentication } from 'app/shared/reducers/authentication';

// Mock translations
const mockTranslations = {
  'trader-help.title': 'How to use RNExchange (Trader)',
  'trader-help.introduction': 'Welcome to RNExchange! This is a simulated trading environment.',
  'trader-help.responsibilities.title': 'Your Responsibilities as a Trader',
  'trader-help.responsibilities.items.0': 'Place orders (buy/sell) in supported instruments',
  'trader-help.responsibilities.items.1': 'Manage your watchlist of instruments to monitor',
  'trader-help.responsibilities.items.2': 'Track your positions and portfolio performance',
  'trader-help.responsibilities.items.3': 'Review your ledger and daily statements',
  'trader-help.responsibilities.items.4': 'Monitor your buying power and account balance',
  'trader-help.screens.title': "Main Screens You'll Use",
  'trader-help.screens.items.0.name': 'Market Watch',
  'trader-help.screens.items.0.description': 'View live market data and place orders',
  'trader-help.screens.items.1.name': 'Portfolio',
  'trader-help.screens.items.1.description': 'See your open positions and P&L',
  'trader-help.screens.items.2.name': 'Ledger',
  'trader-help.screens.items.2.description': 'View all cash movements',
  'trader-help.screens.items.3.name': 'Statements',
  'trader-help.screens.items.3.description': 'Access daily statements',
  'trader-help.flows.title': 'Key Flows You Can Perform',
  'trader-help.flows.items.0.title': 'Add to Watchlist',
  'trader-help.flows.items.0.description': 'Go to Market Watch and add instruments',
  'trader-help.flows.items.1.title': 'Place a Market Order',
  'trader-help.flows.items.1.description': 'Select an instrument and submit an order',
  'trader-help.flows.items.2.title': 'Monitor Execution',
  'trader-help.flows.items.2.description': 'Watch your order fill in real-time',
  'trader-help.flows.items.3.title': 'View Portfolio',
  'trader-help.flows.items.3.description': 'Navigate to Portfolio to see positions',
  'trader-help.flows.items.4.title': 'Check Ledger',
  'trader-help.flows.items.4.description': 'Review cash movements from trades',
  'trader-help.flows.items.5.title': 'Review Statements',
  'trader-help.flows.items.5.description': 'Access daily statements after EOD',
  'trader-help.demoUsers.title': 'Demo Users',
  'trader-help.demoUsers.description': 'For testing, use the trader_demo account.',
  'trader-help.disclaimer': 'Remember: RNExchange is a simulated environment.',
  'broker-help.title': 'How to use RNExchange (Broker Admin)',
  'broker-help.introduction': 'Welcome to RNExchange Broker Admin!',
  'broker-help.responsibilities.title': 'Your Responsibilities as a Broker Admin',
  'broker-help.responsibilities.items.0': 'Manage traders under your broker',
  'broker-help.responsibilities.items.1': 'Post funds journal entries',
  'broker-help.responsibilities.items.2': 'Monitor trader balances',
  'broker-help.responsibilities.items.3': 'Review risk alerts',
  'broker-help.responsibilities.items.4': 'Access trader statements',
  'broker-help.screens.title': "Main Screens You'll Use",
  'broker-help.screens.items.0.name': 'Broker Dashboard',
  'broker-help.screens.items.0.description': 'Overview of active traders',
  'broker-help.screens.items.1.name': 'Clients',
  'broker-help.screens.items.1.description': 'List of all traders',
  'broker-help.screens.items.2.name': 'Funds Journal',
  'broker-help.screens.items.2.description': 'Create credit or debit entries',
  'broker-help.screens.items.3.name': 'Settlements',
  'broker-help.screens.items.3.description': 'View settlement batches',
  'broker-help.flows.title': 'Key Flows You Can Perform',
  'broker-help.flows.items.0.title': 'View Trader List',
  'broker-help.flows.items.0.description': 'Go to Clients to see all traders',
  'broker-help.flows.items.1.title': 'Credit Funds',
  'broker-help.flows.items.1.description': 'Navigate to Funds Journal',
  'broker-help.flows.items.2.title': 'Debit Funds',
  'broker-help.flows.items.2.description': 'Create debit journal entries',
  'broker-help.flows.items.3.title': 'Monitor Account Status',
  'broker-help.flows.items.3.description': 'Check for negative balances',
  'broker-help.flows.items.4.title': 'Review Statements',
  'broker-help.flows.items.4.description': 'Access trader statements',
  'broker-help.flows.items.5.title': 'Risk Management',
  'broker-help.flows.items.5.description': 'Monitor at-risk accounts',
  'broker-help.demoUsers.title': 'Demo Users',
  'broker-help.demoUsers.description': 'For testing, use the broker_demo account.',
  'broker-help.disclaimer': 'Remember: RNExchange is a simulated environment.',
  'exchange-help.title': 'How to use RNExchange (Exchange Operator)',
  'exchange-help.introduction': 'Welcome to RNExchange Exchange Operator!',
  'exchange-help.responsibilities.title': 'Your Responsibilities',
  'exchange-help.responsibilities.items.0': 'Run end-of-day settlement',
  'exchange-help.responsibilities.items.1': 'Monitor exchange health',
  'exchange-help.responsibilities.items.2': 'View settlement batches',
  'exchange-help.responsibilities.items.3': 'Oversee account reconciliation',
  'exchange-help.responsibilities.items.4': 'Access system reports',
  'exchange-help.screens.title': 'Main Screens',
  'exchange-help.screens.items.0.name': 'Exchange Overview',
  'exchange-help.screens.items.0.description': 'Dashboard showing EOD batches',
  'exchange-help.screens.items.1.name': 'Settlement Batches',
  'exchange-help.screens.items.1.description': 'List of EOD runs',
  'exchange-help.screens.items.2.name': 'System Status',
  'exchange-help.screens.items.2.description': 'Monitor exchange health',
  'exchange-help.screens.items.3.name': 'Market Data',
  'exchange-help.screens.items.3.description': 'Control market data feed',
  'exchange-help.flows.title': 'Key Flows',
  'exchange-help.flows.items.0.title': 'Monitor Status',
  'exchange-help.flows.items.0.description': 'Go to Exchange Overview',
  'exchange-help.flows.items.1.title': 'Run EOD',
  'exchange-help.flows.items.1.description': 'Trigger EOD processing',
  'exchange-help.flows.items.2.title': 'Review Batches',
  'exchange-help.flows.items.2.description': 'View settlement details',
  'exchange-help.flows.items.3.title': 'Verify Statements',
  'exchange-help.flows.items.3.description': 'Confirm statement generation',
  'exchange-help.flows.items.4.title': 'Handle Reruns',
  'exchange-help.flows.items.4.description': 'EOD is idempotent',
  'exchange-help.flows.items.5.title': 'System Monitoring',
  'exchange-help.flows.items.5.description': 'Monitor system health',
  'exchange-help.demoUsers.title': 'Demo Users',
  'exchange-help.demoUsers.description': 'For testing, use the exchange_demo account.',
  'exchange-help.disclaimer': 'Remember: RNExchange is a simulated environment.',
};

describe('RoleHelpPanel', () => {
  let store;

  beforeEach(() => {
    store = initStore();
    // Register mock translations
    TranslatorContext.registerTranslations('en', mockTranslations);
    TranslatorContext.setLocale('en');
  });

  const renderHelpPanel = (authorities: string[]) => {
    store.dispatch(
      setAuthentication({
        isAuthenticated: true,
        account: {
          login: 'test-user',
          authorities: authorities,
        },
      }),
    );

    return render(
      <Provider store={store}>
        <MemoryRouter>
          <RoleHelpPanel />
        </MemoryRouter>
      </Provider>,
    );
  };

  describe('Trader Role', () => {
    it('should render help panel for trader role', () => {
      renderHelpPanel([AUTHORITIES.TRADER]);

      expect(screen.getByTestId('role-help-panel')).toBeInTheDocument();
      expect(screen.getByTestId('help-toggle-button')).toBeInTheDocument();
      expect(screen.getByText(/How to use RNExchange \(Trader\)/i)).toBeInTheDocument();
    });

    it('should open and display trader-specific help content when clicked', () => {
      renderHelpPanel([AUTHORITIES.TRADER]);

      const toggleButton = screen.getByTestId('help-toggle-button');
      fireEvent.click(toggleButton);

      expect(screen.getByTestId('help-content')).toBeInTheDocument();
      expect(screen.getByText(/Welcome to RNExchange!/i)).toBeInTheDocument();
      expect(screen.getByText(/Your Responsibilities as a Trader/i)).toBeInTheDocument();
      expect(screen.getByText(/Main Screens You'll Use/i)).toBeInTheDocument();
      expect(screen.getByText(/Key Flows You Can Perform/i)).toBeInTheDocument();
      expect(screen.getByText(/Market Watch/i)).toBeInTheDocument();
      expect(screen.getByText(/Place a Market Order/i)).toBeInTheDocument();
    });

    it('should close help content when close button is clicked', () => {
      renderHelpPanel([AUTHORITIES.TRADER]);

      const toggleButton = screen.getByTestId('help-toggle-button');
      fireEvent.click(toggleButton);

      expect(screen.getByTestId('help-content')).toBeInTheDocument();

      const closeButton = screen.getByLabelText(/Close help/i);
      fireEvent.click(closeButton);

      expect(screen.queryByTestId('help-content')).not.toBeInTheDocument();
    });

    it('should display trader disclaimer', () => {
      renderHelpPanel([AUTHORITIES.TRADER]);

      const toggleButton = screen.getByTestId('help-toggle-button');
      fireEvent.click(toggleButton);

      expect(screen.getByText(/RNExchange is a simulated environment/i)).toBeInTheDocument();
    });
  });

  describe('Broker Admin Role', () => {
    it('should render help panel for broker admin role', () => {
      renderHelpPanel([AUTHORITIES.BROKER_ADMIN]);

      expect(screen.getByTestId('role-help-panel')).toBeInTheDocument();
      expect(screen.getByText(/How to use RNExchange \(Broker Admin\)/i)).toBeInTheDocument();
    });

    it('should display broker-specific help content', () => {
      renderHelpPanel([AUTHORITIES.BROKER_ADMIN]);

      const toggleButton = screen.getByTestId('help-toggle-button');
      fireEvent.click(toggleButton);

      expect(screen.getByText(/Welcome to RNExchange Broker Admin!/i)).toBeInTheDocument();
      expect(screen.getByText(/Your Responsibilities as a Broker Admin/i)).toBeInTheDocument();
      expect(screen.getByText(/Funds Journal/i)).toBeInTheDocument();
      expect(screen.getByText(/Credit Funds/i)).toBeInTheDocument();
    });
  });

  describe('Exchange Operator Role', () => {
    it('should render help panel for exchange operator role', () => {
      renderHelpPanel([AUTHORITIES.EXCHANGE_OPERATOR]);

      expect(screen.getByTestId('role-help-panel')).toBeInTheDocument();
      expect(screen.getByText(/How to use RNExchange \(Exchange Operator\)/i)).toBeInTheDocument();
    });

    it('should display exchange-specific help content', () => {
      renderHelpPanel([AUTHORITIES.EXCHANGE_OPERATOR]);

      const toggleButton = screen.getByTestId('help-toggle-button');
      fireEvent.click(toggleButton);

      expect(screen.getByText(/Welcome to RNExchange Exchange Operator!/i)).toBeInTheDocument();
      expect(screen.getByText(/Run end-of-day settlement/i)).toBeInTheDocument();
      expect(screen.getByText(/Exchange Overview/i)).toBeInTheDocument();
      expect(screen.getByText(/Run EOD/i)).toBeInTheDocument();
    });
  });

  describe('User without valid role', () => {
    it('should not render help panel for user without trader/broker/exchange role', () => {
      renderHelpPanel([AUTHORITIES.USER]);

      expect(screen.queryByTestId('role-help-panel')).not.toBeInTheDocument();
    });
  });

  describe('Multiple Roles', () => {
    it('should prioritize TRADER role when user has multiple roles', () => {
      renderHelpPanel([AUTHORITIES.TRADER, AUTHORITIES.USER]);

      expect(screen.getByText(/How to use RNExchange \(Trader\)/i)).toBeInTheDocument();
    });

    it('should prioritize BROKER_ADMIN over USER role', () => {
      renderHelpPanel([AUTHORITIES.BROKER_ADMIN, AUTHORITIES.USER]);

      expect(screen.getByText(/How to use RNExchange \(Broker Admin\)/i)).toBeInTheDocument();
    });
  });

  describe('Help Content Structure', () => {
    it('should display all required sections for trader', () => {
      renderHelpPanel([AUTHORITIES.TRADER]);

      const toggleButton = screen.getByTestId('help-toggle-button');
      fireEvent.click(toggleButton);

      // Check for all main sections
      expect(screen.getByText(/Your Responsibilities/i)).toBeInTheDocument();
      expect(screen.getByText(/Main Screens/i)).toBeInTheDocument();
      expect(screen.getByText(/Key Flows/i)).toBeInTheDocument();
      expect(screen.getByText(/Demo Users/i)).toBeInTheDocument();
    });

    it('should display responsibilities list items', () => {
      renderHelpPanel([AUTHORITIES.TRADER]);

      const toggleButton = screen.getByTestId('help-toggle-button');
      fireEvent.click(toggleButton);

      expect(screen.getByText(/Place orders \(buy\/sell\)/i)).toBeInTheDocument();
      expect(screen.getByText(/Manage your watchlist/i)).toBeInTheDocument();
    });

    it('should display flows as ordered list', () => {
      renderHelpPanel([AUTHORITIES.TRADER]);

      const toggleButton = screen.getByTestId('help-toggle-button');
      fireEvent.click(toggleButton);

      // Check that flows are displayed (ordered list)
      expect(screen.getByText(/Add to Watchlist/i)).toBeInTheDocument();
      expect(screen.getByText(/Place a Market Order/i)).toBeInTheDocument();
    });
  });
});
