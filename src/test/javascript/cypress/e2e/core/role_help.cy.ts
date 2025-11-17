/**
 * M6 User Story 5, Task T051: Cypress coverage to ensure each role can discover
 * and open their "How to use RNExchange" help from the dashboard.
 * Educational Transparency: discoverability of in-app help.
 */

describe('Role Help Discovery', () => {
  describe('Trader Role Help', () => {
    const username = Cypress.env('TRADER_USERNAME') ?? 'trader_demo';
    const password = Cypress.env('TRADER_PASSWORD') ?? 'password';

    beforeEach(() => {
      cy.login(username, password);
    });

    it('should display help panel on trader dashboard', () => {
      cy.visit('/trader');
      cy.get('[data-cy="role-help-panel"]').should('be.visible');
      cy.get('[data-cy="help-toggle-button"]').should('be.visible');
      cy.get('[data-cy="help-toggle-button"]').should('contain', 'How to use RNExchange');
    });

    it('should open help content when help button is clicked', () => {
      cy.visit('/trader');
      cy.get('[data-cy="help-toggle-button"]').click();
      cy.get('[data-cy="help-content"]').should('be.visible');
      cy.get('[data-cy="help-content"]').should('contain', 'How to use RNExchange (Trader)');
    });

    it('should display trader-specific help content', () => {
      cy.visit('/trader');
      cy.get('[data-cy="help-toggle-button"]').click();

      // Check for trader-specific sections
      cy.get('[data-cy="help-content"]').should('contain', 'Your Responsibilities as a Trader');
      cy.get('[data-cy="help-content"]').should('contain', "Main Screens You'll Use");
      cy.get('[data-cy="help-content"]').should('contain', 'Key Flows You Can Perform');
      cy.get('[data-cy="help-content"]').should('contain', 'Market Watch');
      cy.get('[data-cy="help-content"]').should('contain', 'Portfolio');
      cy.get('[data-cy="help-content"]').should('contain', 'Place a Market Order');
    });

    it('should display educational disclaimer in help content', () => {
      cy.visit('/trader');
      cy.get('[data-cy="help-toggle-button"]').click();

      cy.get('[data-cy="help-content"]').should('contain', 'simulated');
      cy.get('[data-cy="help-content"]').should('contain', 'NOT real money');
    });

    it('should close help content when close button is clicked', () => {
      cy.visit('/trader');
      cy.get('[data-cy="help-toggle-button"]').click();
      cy.get('[data-cy="help-content"]').should('be.visible');

      cy.get('[data-cy="help-content"]').within(() => {
        cy.get('button[aria-label="Close help"]').click();
      });

      cy.get('[data-cy="help-content"]').should('not.exist');
    });

    it('should toggle help content when button is clicked again', () => {
      cy.visit('/trader');
      cy.get('[data-cy="help-toggle-button"]').click();
      cy.get('[data-cy="help-content"]').should('be.visible');

      cy.get('[data-cy="help-toggle-button"]').click();
      cy.get('[data-cy="help-content"]').should('not.exist');

      cy.get('[data-cy="help-toggle-button"]').click();
      cy.get('[data-cy="help-content"]').should('be.visible');
    });
  });

  describe('Broker Admin Role Help', () => {
    const username = Cypress.env('BROKER_ADMIN_USERNAME') ?? 'broker_demo';
    const password = Cypress.env('BROKER_ADMIN_PASSWORD') ?? 'password';

    beforeEach(() => {
      cy.login(username, password);
    });

    it('should display help panel on broker dashboard', () => {
      cy.visit('/broker/dashboard');
      cy.get('[data-cy="role-help-panel"]').should('be.visible');
      cy.get('[data-cy="help-toggle-button"]').should('be.visible');
      cy.get('[data-cy="help-toggle-button"]').should('contain', 'How to use RNExchange');
    });

    it('should display broker-specific help content', () => {
      cy.visit('/broker/dashboard');
      cy.get('[data-cy="help-toggle-button"]').click();

      cy.get('[data-cy="help-content"]').should('contain', 'How to use RNExchange (Broker Admin)');
      cy.get('[data-cy="help-content"]').should('contain', 'Your Responsibilities as a Broker Admin');
      cy.get('[data-cy="help-content"]').should('contain', 'Broker Dashboard');
      cy.get('[data-cy="help-content"]').should('contain', 'Funds Journal');
      cy.get('[data-cy="help-content"]').should('contain', 'Credit Funds');
      cy.get('[data-cy="help-content"]').should('contain', 'Debit Funds');
    });

    it('should reference demo users in broker help content', () => {
      cy.visit('/broker/dashboard');
      cy.get('[data-cy="help-toggle-button"]').click();

      cy.get('[data-cy="help-content"]').should('contain', 'broker_demo');
      cy.get('[data-cy="help-content"]').should('contain', 'Demo Users');
    });
  });

  describe('Exchange Operator Role Help', () => {
    const username = Cypress.env('EXCHANGE_OPERATOR_USERNAME') ?? 'exchange_demo';
    const password = Cypress.env('EXCHANGE_OPERATOR_PASSWORD') ?? 'password';

    beforeEach(() => {
      cy.login(username, password);
    });

    it('should display help panel on exchange console', () => {
      cy.visit('/exchange-console');
      cy.get('[data-cy="role-help-panel"]').should('be.visible');
      cy.get('[data-cy="help-toggle-button"]').should('be.visible');
      cy.get('[data-cy="help-toggle-button"]').should('contain', 'How to use RNExchange');
    });

    it('should display exchange-specific help content', () => {
      cy.visit('/exchange-console');
      cy.get('[data-cy="help-toggle-button"]').click();

      cy.get('[data-cy="help-content"]').should('contain', 'How to use RNExchange (Exchange Operator)');
      cy.get('[data-cy="help-content"]').should('contain', 'Run end-of-day settlement');
      cy.get('[data-cy="help-content"]').should('contain', 'Exchange Overview');
      cy.get('[data-cy="help-content"]').should('contain', 'Run EOD');
      cy.get('[data-cy="help-content"]').should('contain', 'Settlement Batches');
    });

    it('should reference demo users in exchange help content', () => {
      cy.visit('/exchange-console');
      cy.get('[data-cy="help-toggle-button"]').click();

      cy.get('[data-cy="help-content"]').should('contain', 'exchange_demo');
      cy.get('[data-cy="help-content"]').should('contain', 'Demo Users');
    });
  });

  describe('Help Content Accessibility', () => {
    const username = Cypress.env('TRADER_USERNAME') ?? 'trader_demo';
    const password = Cypress.env('TRADER_PASSWORD') ?? 'password';

    beforeEach(() => {
      cy.login(username, password);
    });

    it('should have proper ARIA attributes for accessibility', () => {
      cy.visit('/trader');
      cy.get('[data-cy="help-toggle-button"]').should('have.attr', 'aria-expanded');

      cy.get('[data-cy="help-toggle-button"]').click();
      cy.get('[data-cy="help-toggle-button"]').should('have.attr', 'aria-expanded', 'true');

      cy.get('[data-cy="help-content"]').within(() => {
        cy.get('button[aria-label="Close help"]').should('exist');
      });
    });

    it('should be keyboard accessible', () => {
      cy.visit('/trader');
      cy.get('[data-cy="help-toggle-button"]').focus().should('be.focused');
      cy.get('[data-cy="help-toggle-button"]').type('{enter}');
      cy.get('[data-cy="help-content"]').should('be.visible');
    });
  });

  describe('Help Content Educational Transparency', () => {
    const username = Cypress.env('TRADER_USERNAME') ?? 'trader_demo';
    const password = Cypress.env('TRADER_PASSWORD') ?? 'password';

    beforeEach(() => {
      cy.login(username, password);
    });

    it('should clearly indicate simulated nature in help content', () => {
      cy.visit('/trader');
      cy.get('[data-cy="help-toggle-button"]').click();

      // Check for educational transparency messages
      cy.get('[data-cy="help-content"]').should('contain', 'simulated');
      cy.get('[data-cy="help-content"]').should('contain', 'educational');
      cy.get('[data-cy="help-content"]').should('contain', 'NOT real money');
    });

    it('should display disclaimer prominently', () => {
      cy.visit('/trader');
      cy.get('[data-cy="help-toggle-button"]').click();

      // Disclaimer should be visible in the help content
      cy.get('[data-cy="help-content"]').within(() => {
        cy.get('.help-disclaimer').should('be.visible');
        cy.get('.help-disclaimer').should('contain', 'simulated');
      });
    });
  });
});
