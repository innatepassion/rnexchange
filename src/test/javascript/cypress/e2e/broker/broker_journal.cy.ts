/**
 * M6 User Story 2 (T024): Cypress broker flow covering funds credit/debit entry
 * and UI flagging of negative/at-risk accounts.
 */
describe('Broker Funds Journal', () => {
  const username = Cypress.env('BROKER_ADMIN_USERNAME') ?? 'broker_demo';
  const password = Cypress.env('BROKER_ADMIN_PASSWORD') ?? 'password';

  beforeEach(() => {
    cy.login(username, password);
  });

  it('should allow broker to create credit journal entry', () => {
    cy.visit('/broker/journal');
    cy.get('[data-cy="broker-journal-page"]').should('be.visible');

    // Select a trader
    cy.get('[data-cy="journal-trader-select"]').select(1);
    cy.wait(500); // Wait for trader info to load

    // Fill in credit form
    cy.get('[data-cy="journal-direction"]').select('credit');
    cy.get('[data-cy="journal-amount"]').type('100.00');
    cy.get('[data-cy="journal-reason"]').type('Test credit entry');

    // Submit
    cy.get('[data-cy="journal-submit"]').click();

    // Verify success
    cy.get('[data-cy="journal-success"]').should('be.visible').and('contain', 'Journal entry created successfully');
  });

  it('should allow broker to create debit journal entry', () => {
    cy.visit('/broker/journal');
    cy.get('[data-cy="broker-journal-page"]').should('be.visible');

    // Select a trader
    cy.get('[data-cy="journal-trader-select"]').select(1);
    cy.wait(500);

    // Fill in debit form
    cy.get('[data-cy="journal-direction"]').select('debit');
    cy.get('[data-cy="journal-amount"]').type('50.00');
    cy.get('[data-cy="journal-reason"]').type('Test debit entry');

    // Submit
    cy.get('[data-cy="journal-submit"]').click();

    // Verify success
    cy.get('[data-cy="journal-success"]').should('be.visible').and('contain', 'Journal entry created successfully');
  });

  it('should flag negative balance in UI when debit results in negative', () => {
    cy.visit('/broker/journal');
    cy.get('[data-cy="broker-journal-page"]').should('be.visible');

    // Select a trader
    cy.get('[data-cy="journal-trader-select"]').select(1);
    cy.wait(500);

    // Get current balance
    let currentBalance = 0;
    cy.get('.trader-info .balance')
      .invoke('text')
      .then(text => {
        const match = text.match(/Current Balance: ([\d.-]+)/);
        if (match) {
          currentBalance = parseFloat(match[1]);
        }
      });

    // Create a debit that will result in negative balance
    const debitAmount = currentBalance + 100;
    cy.get('[data-cy="journal-direction"]').select('debit');
    cy.get('[data-cy="journal-amount"]').type(debitAmount.toString());
    cy.get('[data-cy="journal-reason"]').type('Debit to test negative balance flagging');

    // Submit
    cy.get('[data-cy="journal-submit"]').click();

    // Wait for success and refresh
    cy.get('[data-cy="journal-success"]').should('be.visible');
    cy.wait(1000); // Wait for trader list refresh

    // Re-select the trader to see updated balance
    cy.get('[data-cy="journal-trader-select"]').select(1);
    cy.wait(500);

    // Verify negative balance flag is shown
    cy.get('[data-cy="negative-balance-flag"]').should('be.visible').and('contain', 'NEGATIVE');
    cy.get('[data-cy="at-risk-warning"]').should('be.visible').and('contain', 'Account is at risk');
  });

  it('should validate required fields', () => {
    cy.visit('/broker/journal');
    cy.get('[data-cy="broker-journal-page"]').should('be.visible');

    // Try to submit without selecting trader
    cy.get('[data-cy="journal-submit"]').click();

    // Should show validation error (browser native or custom)
    cy.get('[data-cy="journal-trader-select"]:invalid').should('exist');
  });
});
