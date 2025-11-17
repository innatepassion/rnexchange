/**
 * M6 User Story 4, Task T040: Cypress tests for RNExchange-branded landing page
 * and role-specific default landings.
 */

describe('RNExchange Landing Page and Role-Based Navigation', () => {
  const traderUsername = Cypress.env('E2E_TRADER_USERNAME') ?? 'trader_demo';
  const traderPassword = Cypress.env('E2E_TRADER_PASSWORD') ?? 'trader_demo';
  const brokerUsername = Cypress.env('E2E_BROKER_USERNAME') ?? 'broker_demo';
  const brokerPassword = Cypress.env('E2E_BROKER_PASSWORD') ?? 'broker_demo';
  const exchangeUsername = Cypress.env('E2E_EXCHANGE_USERNAME') ?? 'exchange_demo';
  const exchangePassword = Cypress.env('E2E_EXCHANGE_PASSWORD') ?? 'exchange_demo';

  beforeEach(() => {
    cy.intercept('POST', '/api/authenticate').as('authenticate');
  });

  describe('Landing Page', () => {
    it('should display RNExchange branding on landing page', () => {
      cy.visit('/');

      // Check for RNExchange branding elements
      cy.contains('RNExchange', { matchCase: false }).should('be.visible');

      // Check for logo (if present)
      cy.get('img[alt*="RNExchange"], img[alt*="RNX"], .rnexchange-logo').should('exist').or('contain', 'RNX');

      // Check for description of the simulator
      cy.contains(/simulator|simulated|trading/i).should('be.visible');

      // Should NOT show generic JHipster welcome content
      cy.contains('Welcome, Java Hipster!').should('not.exist');
      cy.contains('JHipster homepage').should('not.exist');
    });

    it('should show role-based CTAs for login', () => {
      cy.visit('/');

      // Check for login CTAs for different roles
      cy.contains(/trader|broker|exchange/i).should('be.visible');

      // Check for login link/button
      cy.contains(/sign in|log in|login/i).should('be.visible');
    });
  });

  describe('Role-Based Default Landings', () => {
    it('should redirect trader to Market Watch after login', () => {
      cy.visit('/');
      cy.clickOnLoginItem();

      cy.get('[data-cy="username"]').type(traderUsername);
      cy.get('[data-cy="password"]').type(traderPassword);
      cy.get('button[type="submit"]').click();

      cy.wait('@authenticate').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });

      // Should land on Market Watch
      cy.url().should('include', '/market-watch');
      cy.contains(/market watch|watchlist/i).should('be.visible');
    });

    it('should redirect broker admin to Broker Dashboard after login', () => {
      cy.visit('/');
      cy.clickOnLoginItem();

      cy.get('[data-cy="username"]').type(brokerUsername);
      cy.get('[data-cy="password"]').type(brokerPassword);
      cy.get('button[type="submit"]').click();

      cy.wait('@authenticate').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });

      // Should land on Broker Dashboard
      cy.url().should('include', '/broker/dashboard');
      cy.contains(/broker|dashboard/i).should('be.visible');
    });

    it('should redirect exchange operator to Exchange Overview after login', () => {
      cy.visit('/');
      cy.clickOnLoginItem();

      cy.get('[data-cy="username"]').type(exchangeUsername);
      cy.get('[data-cy="password"]').type(exchangePassword);
      cy.get('button[type="submit"]').click();

      cy.wait('@authenticate').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });

      // Should land on Exchange Overview/Console
      cy.url().then(url => {
        const isExpectedUrl = ['/exchange', '/exchange-console', '/exchange/overview'].some(path => url.includes(path));
        if (!isExpectedUrl) {
          throw new Error(`Expected exchange operator to land on exchange console/overview, but was at: ${url}`);
        }
      });
      cy.contains(/exchange|overview|console/i).should('be.visible');
    });
  });

  describe('Navigation Menu Visibility', () => {
    it('should hide generic JHipster menus for trader', () => {
      cy.login(traderUsername, traderPassword);
      cy.visit('/market-watch');

      // Generic JHipster menus should NOT be visible
      cy.contains('Entities').should('not.exist');
      cy.contains('Administration').should('not.exist');
      cy.contains('Performance').should('not.exist');

      // Trader-specific menus should be visible
      cy.contains(/market|watch|portfolio|ledger|statements/i).should('be.visible');
    });

    it('should hide generic JHipster menus for broker admin', () => {
      cy.login(brokerUsername, brokerPassword);
      cy.visit('/broker/dashboard');

      // Generic JHipster menus should NOT be visible
      cy.contains('Entities').should('not.exist');
      cy.contains('Administration').should('not.exist');
      cy.contains('Performance').should('not.exist');

      // Broker-specific menus should be visible
      cy.contains(/broker|journal|settlements/i).should('be.visible');
    });

    it('should show only relevant admin items for exchange operator', () => {
      cy.login(exchangeUsername, exchangePassword);
      cy.visit('/exchange-console');

      // Generic Entities menu should NOT be visible
      cy.contains('Entities').should('not.exist');

      // Exchange Overview should be visible
      cy.contains(/exchange|overview|console|settlement|eod/i).should('be.visible');
    });
  });
});
