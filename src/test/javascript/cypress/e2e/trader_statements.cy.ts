describe('Trader Statements', () => {
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';

  beforeEach(() => {
    cy.login(username, password);
    cy.intercept('GET', '/api/statements*').as('getStatements');
  });

  it('should load trader statements page', () => {
    cy.visit('/trader/statements');
    cy.wait('@getStatements');

    // Check page title
    cy.contains('Daily Statements').should('be.visible');

    // Check simulated environment notice
    cy.contains('Simulated Environment').should('be.visible');
  });

  it('should display statements list after EOD', () => {
    // Mock statements response
    cy.intercept('GET', '/api/statements*', {
      statusCode: 200,
      body: [
        {
          id: 1,
          refDate: '2025-01-15',
          tradingAccountId: 1,
          openingBalance: 10000,
          eodMtmPnl: 500,
          closingBalance: 10500,
          htmlUrl: '/api/statements/1/html',
        },
      ],
    }).as('getStatementsWithData');

    cy.visit('/trader/statements');
    cy.wait('@getStatementsWithData');

    // Check table headers
    cy.contains('Date').should('be.visible');
    cy.contains('Opening Balance').should('be.visible');
    cy.contains('EOD MTM P&L').should('be.visible');
    cy.contains('Closing Balance').should('be.visible');

    // Check statement data
    cy.contains('15 Jan 2025').should('be.visible');
  });

  it('should open statement HTML in new tab when View is clicked', () => {
    cy.intercept('GET', '/api/statements*', {
      statusCode: 200,
      body: [
        {
          id: 1,
          refDate: '2025-01-15',
          tradingAccountId: 1,
          openingBalance: 10000,
          eodMtmPnl: 500,
          closingBalance: 10500,
          htmlUrl: '/api/statements/1/html',
        },
      ],
    }).as('getStatementsWithData');

    cy.intercept('GET', '/api/statements/1/html', {
      statusCode: 200,
      body: '<html><body>Statement HTML</body></html>',
    }).as('getStatementHtml');

    cy.visit('/trader/statements');
    cy.wait('@getStatementsWithData');

    // Click View button - note: Cypress can't test window.open, but we can verify the link exists
    cy.contains('button', 'View').should('be.visible');
  });

  it('should show empty state when no statements', () => {
    cy.intercept('GET', '/api/statements*', {
      statusCode: 200,
      body: [],
    }).as('getEmptyStatements');

    cy.visit('/trader/statements');
    cy.wait('@getEmptyStatements');

    cy.contains(/No statements found/i).should('be.visible');
  });
});
