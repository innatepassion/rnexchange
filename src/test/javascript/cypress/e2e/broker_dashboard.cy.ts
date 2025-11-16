describe('Broker Dashboard', () => {
  beforeEach(() => {
    const username = Cypress.env('E2E_USERNAME') ?? 'broker_admin';
    const password = Cypress.env('E2E_PASSWORD') ?? 'admin';
    cy.login(username, password);
  });

  it('loads dashboard page and shows cards', () => {
    cy.visit('/broker/dashboard');
    cy.contains('Broker Dashboard');
    cy.contains('Active Traders');
    cy.contains('Total Cash');
    cy.contains('Total Exposure');
  });
});
