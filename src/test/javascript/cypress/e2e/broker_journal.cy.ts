describe('Broker Journal Flow', () => {
  beforeEach(() => {
    // Assume logged-in session is handled by existing Cypress commands/setup in project
    cy.visit('/broker/clients');
  });

  it('opens details and submits a credit journal', () => {
    // Open the first details drawer
    cy.contains('Details').first().click();
    // Fill form
    cy.get('[data-cy=journal-direction]').select('credit');
    cy.get('[data-cy=journal-amount]').clear();
    cy.get('[data-cy=journal-amount]').type('25.00');
    cy.get('[data-cy=journal-reason]').clear();
    cy.get('[data-cy=journal-reason]').type('Cypress credit');
    cy.get('[data-cy=journal-submit]').click();
    // Expect drawer still visible and ledger table present
    cy.contains('Recent Ledger');
  });
});
