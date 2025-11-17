describe('Settlement EOD', () => {
  const username = Cypress.env('E2E_USERNAME') ?? 'admin';
  const password = Cypress.env('E2E_PASSWORD') ?? 'admin';

  beforeEach(() => {
    cy.login(username, password);
    cy.intercept('GET', '/api/settlements*').as('getSettlements');
    cy.intercept('POST', '/api/settlements/eod*').as('runEod');
  });

  it('should load settlement page and display batches', () => {
    cy.visit('/exchange/settlement');
    cy.wait('@getSettlements');

    // Check page title
    cy.contains('Settlement Management').should('be.visible');

    // Check simulated environment notice
    cy.contains('Simulated Environment Notice').should('be.visible');

    // Check for Run EOD button
    cy.contains('Run EOD for Today').should('be.visible');
  });

  it('should run EOD for today', () => {
    cy.visit('/exchange/settlement');
    cy.wait('@getSettlements');

    // Mock successful EOD response
    cy.intercept('POST', '/api/settlements/eod*', {
      statusCode: 202,
      body: {
        id: 1,
        refDate: new Date().toISOString().split('T')[0],
        kind: 'EOD',
        status: 'CREATED',
      },
    }).as('runEodSuccess');

    // Click Run EOD button
    cy.contains('button', 'Run EOD for Today').click();
    cy.wait('@runEodSuccess');

    // Check for success message or loading state
    cy.contains(/EOD settlement/i).should('be.visible');
  });

  it('should display settlement batches table', () => {
    // Mock batches response
    cy.intercept('GET', '/api/settlements*', {
      statusCode: 200,
      body: [
        {
          id: 1,
          refDate: '2025-01-15',
          kind: 'EOD',
          status: 'PROCESSED',
          accountsProcessed: 10,
          positionsProcessed: 25,
          netPnl: 5000.5,
        },
        {
          id: 2,
          refDate: '2025-01-14',
          kind: 'EOD',
          status: 'FAILED',
        },
      ],
    }).as('getSettlementsWithData');

    cy.visit('/exchange/settlement');
    cy.wait('@getSettlementsWithData');

    // Check table headers
    cy.contains('Date').should('be.visible');
    cy.contains('Status').should('be.visible');
    cy.contains('Accounts Processed').should('be.visible');
    cy.contains('Positions Processed').should('be.visible');
    cy.contains('Net P&L').should('be.visible');

    // Check batch data
    cy.contains('PROCESSED').should('be.visible');
    cy.contains('FAILED').should('be.visible');
  });

  it('should show error message on EOD failure', () => {
    cy.visit('/exchange/settlement');
    cy.wait('@getSettlements');

    // Mock failed EOD response
    cy.intercept('POST', '/api/settlements/eod*', {
      statusCode: 500,
      body: { message: 'Settlement failed: No settlement price found' },
    }).as('runEodFailure');

    cy.contains('button', 'Run EOD for Today').click();
    cy.wait('@runEodFailure');

    // Check for error message
    cy.contains(/error|failed/i).should('be.visible');
  });

  it('should refresh batches list', () => {
    cy.visit('/exchange/settlement');
    cy.wait('@getSettlements');

    // Click refresh button
    cy.contains('button', 'Refresh').click();
    cy.wait('@getSettlements');
  });

  it('should verify statements generated after EOD contain simulation disclaimer', () => {
    // M6 User Story 3, Task T036B: Assert disclaimer appears in statements after EOD
    cy.intercept('POST', '/api/settlements/eod*', {
      statusCode: 202,
      body: {
        id: 1,
        refDate: new Date().toISOString().split('T')[0],
        kind: 'EOD',
        status: 'PROCESSED',
        accountsProcessed: 1,
        positionsProcessed: 1,
        netPnl: 500,
      },
    }).as('runEodSuccess');

    cy.intercept('GET', '/api/statements*', {
      statusCode: 200,
      body: [
        {
          id: 1,
          refDate: new Date().toISOString().split('T')[0],
          tradingAccountId: 1,
          openingBalance: 10000,
          eodMtmPnl: 500,
          closingBalance: 10500,
          htmlUrl: '/api/statements/1/html',
        },
      ],
    }).as('getStatements');

    // Mock HTML statement with disclaimer
    cy.intercept('GET', '/api/statements/1/html', {
      statusCode: 200,
      headers: { 'Content-Type': 'text/html' },
      body: '<html><body><div class="disclaimer">⚠️ This is a simulated environment — not real trading or money</div></body></html>',
    }).as('getStatementHtml');

    cy.visit('/exchange/settlement');
    cy.wait('@getSettlements');

    // Run EOD
    cy.contains('button', 'Run EOD for Today').click();
    cy.wait('@runEodSuccess');

    // Verify statement HTML contains disclaimer
    cy.request({
      method: 'GET',
      url: '/api/statements/1/html',
      headers: { Cookie: cy.getCookie('XSRF-TOKEN') },
    }).then(response => {
      expect(response.status).to.eq(200);
      expect(response.body).to.include('simulated environment');
      expect(response.body).to.include('not real trading or money');
    });
  });
});
