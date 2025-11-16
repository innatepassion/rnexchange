describe('Broker Clients E2E', () => {
  beforeEach(() => {
    // assumes a helper custom command cy.login exists or a login form is available at /login
    cy.visit('/');
  });

  it('should navigate to broker clients and open details drawer', () => {
    // Navigate directly; route is protected, app should redirect to login if needed
    cy.visit('/broker/clients');

    // Wait for table to render or empty state
    cy.contains('Clients', { timeout: 10000 }).should('be.visible');

    // If rows exist, click Details on the first row
    cy.get('table tbody tr').then($rows => {
      if ($rows.length > 0) {
        cy.wrap($rows[0]).find('button').contains('Details').click();
        // Drawer should appear
        cy.get('.drawer').should('be.visible');
        cy.get('.drawer').contains('Trader Details').should('be.visible');
        // Close
        cy.get('.drawer button').contains('Close').click();
        cy.get('.drawer').should('not.exist');
      }
    });
  });
});
