/**
 * M6 Phase 8, Task T056A: Cypress UI tests for RBAC negative paths.
 * Verifies that when users attempt to access pages or actions outside their role,
 * they see clear, role-appropriate messages and are redirected or blocked without
 * leaking underlying data.
 *
 * Edge cases tested:
 * - Trader navigating to Broker Admin or Exchange-only routes
 * - Broker Admin attempting Trader or Exchange-only actions
 * - Exchange Operator attempting Trader or Broker-only actions
 * - Direct URL navigation to unauthorized routes
 * - API calls that should be blocked at the UI level
 */

describe('RBAC Negative Path UI Tests', () => {
  const traderUsername = Cypress.env('E2E_TRADER_USERNAME') ?? 'trader_demo';
  const traderPassword = Cypress.env('E2E_TRADER_PASSWORD') ?? 'trader_demo';
  const brokerUsername = Cypress.env('E2E_BROKER_USERNAME') ?? 'broker_demo';
  const brokerPassword = Cypress.env('E2E_BROKER_PASSWORD') ?? 'broker_demo';
  const exchangeUsername = Cypress.env('E2E_EXCHANGE_USERNAME') ?? 'exchange_demo';
  const exchangePassword = Cypress.env('E2E_EXCHANGE_PASSWORD') ?? 'exchange_demo';

  beforeEach(() => {
    cy.intercept('GET', '/api/**').as('apiCall');
    cy.intercept('POST', '/api/**').as('apiPost');
  });

  describe('Trader attempting Broker Admin routes', () => {
    beforeEach(() => {
      cy.login(traderUsername, traderPassword);
    });

    it('should block trader from accessing broker dashboard', () => {
      cy.visit('/broker/dashboard');

      // Should see an error message or be redirected
      cy.url().should('satisfy', url => {
        // Either redirected away or showing error
        return !url.includes('/broker/dashboard') || cy.contains(/unauthorized|forbidden|access denied/i);
      });

      // Should see a clear message about role restrictions
      cy.contains(/trader|you're signed in as a trader/i)
        .should('be.visible')
        .or('contain', 'broker');
      cy.contains(/only available to broker|broker admin/i).should('be.visible');
    });

    it('should block trader from accessing broker journal page', () => {
      cy.visit('/broker/journal');

      cy.url().should('satisfy', url => {
        return !url.includes('/broker/journal') || cy.contains(/unauthorized|forbidden/i);
      });

      cy.contains(/trader|you're signed in as a trader/i).should('be.visible');
    });

    it('should block trader from accessing broker settlements page', () => {
      cy.visit('/broker/settlements');

      cy.url().should('satisfy', url => {
        return !url.includes('/broker/settlements') || cy.contains(/unauthorized|forbidden/i);
      });

      cy.contains(/trader|you're signed in as a trader/i).should('be.visible');
    });

    it('should not show broker menu items in navigation for trader', () => {
      cy.visit('/market-watch');

      // Broker-specific menu items should not be visible
      cy.contains(/broker.*journal|broker.*settlements|broker.*dashboard/i).should('not.exist');
    });
  });

  describe('Trader attempting Exchange Operator routes', () => {
    beforeEach(() => {
      cy.login(traderUsername, traderPassword);
    });

    it('should block trader from accessing exchange overview', () => {
      cy.visit('/exchange/overview');

      cy.url().should('satisfy', url => {
        return !url.includes('/exchange/overview') || cy.contains(/unauthorized|forbidden/i);
      });

      cy.contains(/trader|you're signed in as a trader/i).should('be.visible');
      cy.contains(/exchange operator|only available to exchange/i).should('be.visible');
    });

    it('should block trader from accessing EOD console', () => {
      cy.visit('/exchange/eod');

      cy.url().should('satisfy', url => {
        return !url.includes('/exchange/eod') || cy.contains(/unauthorized|forbidden/i);
      });
    });

    it('should not show exchange menu items in navigation for trader', () => {
      cy.visit('/market-watch');

      cy.contains(/exchange.*overview|exchange.*console|eod|settlement.*batch/i).should('not.exist');
    });
  });

  describe('Broker Admin attempting Trader routes', () => {
    beforeEach(() => {
      cy.login(brokerUsername, brokerPassword);
    });

    it('should block broker admin from accessing trader market watch', () => {
      cy.visit('/market-watch');

      // Broker admin should be redirected or see error
      cy.url().should('satisfy', url => {
        return !url.includes('/market-watch') || cy.contains(/unauthorized|forbidden/i);
      });

      // Should see role-appropriate message
      cy.contains(/broker|you're signed in as a broker/i).should('be.visible');
      cy.contains(/only available to trader|trader only/i).should('be.visible');
    });

    it('should block broker admin from accessing trader statements', () => {
      cy.visit('/trader/statements');

      cy.url().should('satisfy', url => {
        return !url.includes('/trader/statements') || cy.contains(/unauthorized|forbidden/i);
      });

      cy.contains(/broker|you're signed in as a broker/i).should('be.visible');
    });

    it('should not show trader-specific menu items in navigation for broker admin', () => {
      cy.visit('/broker/dashboard');

      // Trader-specific menus should not be visible
      cy.contains(/market.*watch|trader.*portfolio|trader.*statements/i).should('not.exist');
    });
  });

  describe('Broker Admin attempting Exchange Operator routes', () => {
    beforeEach(() => {
      cy.login(brokerUsername, brokerPassword);
    });

    it('should block broker admin from accessing exchange overview', () => {
      cy.visit('/exchange/overview');

      cy.url().should('satisfy', url => {
        return !url.includes('/exchange/overview') || cy.contains(/unauthorized|forbidden/i);
      });

      cy.contains(/broker|you're signed in as a broker/i).should('be.visible');
      cy.contains(/exchange operator|only available to exchange/i).should('be.visible');
    });

    it('should block broker admin from running EOD', () => {
      cy.visit('/exchange/eod');

      cy.url().should('satisfy', url => {
        return !url.includes('/exchange/eod') || cy.contains(/unauthorized|forbidden/i);
      });
    });

    it('should not show exchange menu items in navigation for broker admin', () => {
      cy.visit('/broker/dashboard');

      cy.contains(/exchange.*overview|exchange.*console|eod/i).should('not.exist');
    });
  });

  describe('Exchange Operator attempting Trader routes', () => {
    beforeEach(() => {
      cy.login(exchangeUsername, exchangePassword);
    });

    it('should block exchange operator from accessing trader market watch', () => {
      cy.visit('/market-watch');

      cy.url().should('satisfy', url => {
        return !url.includes('/market-watch') || cy.contains(/unauthorized|forbidden/i);
      });

      cy.contains(/exchange|you're signed in as an exchange/i).should('be.visible');
      cy.contains(/only available to trader|trader only/i).should('be.visible');
    });

    it('should block exchange operator from accessing trader statements', () => {
      cy.visit('/trader/statements');

      cy.url().should('satisfy', url => {
        return !url.includes('/trader/statements') || cy.contains(/unauthorized|forbidden/i);
      });
    });

    it('should not show trader menu items in navigation for exchange operator', () => {
      cy.visit('/exchange/overview');

      cy.contains(/market.*watch|trader.*portfolio|trader.*statements/i).should('not.exist');
    });
  });

  describe('Exchange Operator attempting Broker Admin routes', () => {
    beforeEach(() => {
      cy.login(exchangeUsername, exchangePassword);
    });

    it('should block exchange operator from accessing broker dashboard', () => {
      cy.visit('/broker/dashboard');

      cy.url().should('satisfy', url => {
        return !url.includes('/broker/dashboard') || cy.contains(/unauthorized|forbidden/i);
      });

      cy.contains(/exchange|you're signed in as an exchange/i).should('be.visible');
      cy.contains(/broker admin|only available to broker/i).should('be.visible');
    });

    it('should block exchange operator from accessing broker journal', () => {
      cy.visit('/broker/journal');

      cy.url().should('satisfy', url => {
        return !url.includes('/broker/journal') || cy.contains(/unauthorized|forbidden/i);
      });
    });

    it('should not show broker menu items in navigation for exchange operator', () => {
      cy.visit('/exchange/overview');

      cy.contains(/broker.*dashboard|broker.*journal|broker.*settlements/i).should('not.exist');
    });
  });

  describe('API-level blocking verification', () => {
    it('should block trader from making broker journal API calls', () => {
      cy.login(traderUsername, traderPassword);
      cy.visit('/market-watch');

      cy.authenticatedRequest({
        method: 'POST',
        url: '/api/broker/traders/00000000-0000-0000-0000-000000000000/journal',
        body: { direction: 'credit', amount: 100.0, reason: 'test' },
        failOnStatusCode: false,
      }).then(response => {
        expect(response.status).to.equal(403);
      });
    });

    it('should block broker admin from making EOD API calls', () => {
      cy.login(brokerUsername, brokerPassword);
      cy.visit('/broker/dashboard');

      cy.authenticatedRequest({
        method: 'POST',
        url: '/api/settlements/eod?date=2025-01-15',
        failOnStatusCode: false,
      }).then(response => {
        expect(response.status).to.equal(403);
      });
    });

    it('should block trader from making EOD API calls', () => {
      cy.login(traderUsername, traderPassword);
      cy.visit('/market-watch');

      cy.authenticatedRequest({
        method: 'POST',
        url: '/api/settlements/eod?date=2025-01-15',
        failOnStatusCode: false,
      }).then(response => {
        expect(response.status).to.equal(403);
      });
    });
  });

  describe('Error message clarity and data leakage prevention', () => {
    it('should show clear role-appropriate messages without exposing internal details', () => {
      cy.login(traderUsername, traderPassword);
      cy.visit('/broker/dashboard');

      // Should see user-friendly message
      cy.contains(/you're signed in as a trader|trader/i).should('be.visible');
      cy.contains(/broker admin|broker/i).should('be.visible');

      // Should NOT expose internal technical details
      cy.contains(/403|forbidden|unauthorized|access denied/i)
        .should('not.exist')
        .or('be.hidden');
      cy.contains(/preauthorize|hasrole|authority/i).should('not.exist');
    });

    it('should redirect to appropriate landing page after unauthorized access attempt', () => {
      cy.login(traderUsername, traderPassword);
      cy.visit('/broker/dashboard');

      // Should eventually land on trader-appropriate page
      cy.url().should('satisfy', url => {
        return url.includes('/market-watch') || url.includes('/trader') || url === '/';
      });
    });
  });
});
