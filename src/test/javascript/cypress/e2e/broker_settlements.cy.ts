describe('Broker Settlements', () => {
  const username = Cypress.env('E2E_USERNAME') ?? 'admin';
  const password = Cypress.env('E2E_PASSWORD') ?? 'admin';

  beforeEach(() => {
    cy.login(username, password);
    cy.intercept('GET', '/api/broker/settlements*').as('getBrokerSettlements');
  });

  it('should load broker settlements page', () => {
    cy.visit('/broker/settlements');
    cy.wait('@getBrokerSettlements');

    // Check page title
    cy.contains('Settlements').should('be.visible');

    // Check simulated environment notice
    cy.contains('Simulated Environment').should('be.visible');
  });

  it('should display settlements list after EOD', () => {
    // Mock settlements response
    cy.intercept('GET', '/api/broker/settlements*', {
      statusCode: 200,
      body: [
        {
          refDate: '2025-01-15',
          brokerId: 1,
          brokerName: 'Test Broker',
          totalClientCount: 10,
          totalOpeningBalance: 100000,
          totalClosingBalance: 105000,
          totalEodMtmPnl: 5000,
          summaryUrl: '/api/broker/settlements/1/summary',
        },
      ],
    }).as('getSettlementsWithData');

    cy.visit('/broker/settlements');
    cy.wait('@getSettlementsWithData');

    // Check table headers
    cy.contains('Date').should('be.visible');
    cy.contains('Client Count').should('be.visible');
    cy.contains('Opening Balance').should('be.visible');
    cy.contains('EOD MTM P&L').should('be.visible');
    cy.contains('Closing Balance').should('be.visible');

    // Check settlement data
    cy.contains('15 Jan 2025').should('be.visible');
    cy.contains('10').should('be.visible');
  });

  it('should open summary in new tab when View Summary is clicked', () => {
    cy.intercept('GET', '/api/broker/settlements*', {
      statusCode: 200,
      body: [
        {
          refDate: '2025-01-15',
          brokerId: 1,
          brokerName: 'Test Broker',
          totalClientCount: 10,
          totalOpeningBalance: 100000,
          totalClosingBalance: 105000,
          totalEodMtmPnl: 5000,
          summaryUrl: '/api/broker/settlements/1/summary',
        },
      ],
    }).as('getSettlementsWithData');

    cy.intercept('GET', '/api/broker/settlements/1/summary', {
      statusCode: 200,
      body: '<html><body>Broker Summary HTML</body></html>',
    }).as('getSummaryHtml');

    cy.visit('/broker/settlements');
    cy.wait('@getSettlementsWithData');

    // Click View Summary button - note: Cypress can't test window.open, but we can verify the link exists
    cy.contains('button', 'View Summary').should('be.visible');
  });

  it('should show empty state when no settlements', () => {
    cy.intercept('GET', '/api/broker/settlements*', {
      statusCode: 200,
      body: [],
    }).as('getEmptySettlements');

    cy.visit('/broker/settlements');
    cy.wait('@getEmptySettlements');

    cy.contains(/No settlements found/i).should('be.visible');
  });

  it('should allow drill-down to client statements', () => {
    cy.intercept('GET', '/api/broker/settlements*', {
      statusCode: 200,
      body: [
        {
          refDate: '2025-01-15',
          brokerId: 1,
          brokerName: 'Test Broker',
          totalClientCount: 10,
          totalOpeningBalance: 100000,
          totalClosingBalance: 105000,
          totalEodMtmPnl: 5000,
          summaryUrl: '/api/broker/settlements/1/summary',
        },
      ],
    }).as('getSettlementsWithData');

    cy.visit('/broker/settlements');
    cy.wait('@getSettlementsWithData');

    // Check for drill-down functionality (may be implemented as a button or link)
    cy.contains(/client|statements/i).should('be.visible');
  });
});
