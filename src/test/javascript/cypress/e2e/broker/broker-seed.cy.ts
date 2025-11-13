describe('Broker baseline readiness', () => {
  const username = Cypress.env('BROKER_ADMIN_USERNAME') ?? 'broker-admin';
  const password = Cypress.env('BROKER_ADMIN_PASSWORD') ?? 'password';

  let brokerId = 0;

  before(() => {
    cy.login(username, password);
    cy.authenticatedRequest({
      method: 'GET',
      url: '/api/brokers?sort=id,asc',
    }).then(({ body }) => {
      const broker = body.find((candidate: { code?: string }) => candidate.code === 'RN_DEMO');
      if (!broker?.id) {
        throw new Error('Expected seeded broker RN_DEMO to be available');
      }
      brokerId = broker.id;
    });
  });

  beforeEach(() => {
    cy.login(username, password);
  });

  it('captures login-to-ready duration and validates seeded broker catalog', () => {
    if (!brokerId) {
      throw new Error('brokerId not initialised');
    }

    cy.intercept('GET', `/api/brokers/${brokerId}`).as('brokerRequest');
    cy.intercept('GET', `/api/brokers/${brokerId}/baseline`).as('brokerBaselineRequest');

    let loginToReadyStart = 0;
    let instrumentCount = 0;

    cy.then(() => {
      loginToReadyStart = Date.now();
    });

    cy.visit(`/broker/${brokerId}`);
    cy.wait('@brokerRequest');
    cy.wait('@brokerBaselineRequest');

    cy.get('[data-cy="broker-baseline-name"]').should('contain', 'RN DEMO BROKING');
    cy.get('[data-cy="broker-baseline-code"]').should('contain', 'RN_DEMO');
    cy.get('[data-cy="broker-exchange-membership"]').should('contain', 'NSE');
    cy.get('[data-cy="broker-exchange-membership"]').should('contain', 'BSE');
    cy.get('[data-cy="broker-exchange-membership"]').should('contain', 'MCX');
    cy.get('[data-cy="broker-admin-login"]').should('contain', 'broker-admin');

    cy.get('[data-cy="broker-instrument-row"]')
      .should('have.length', 11)
      .then($rows => {
        instrumentCount = $rows.length;
      });

    cy.contains('[data-cy="broker-instrument-row"]', 'RELIANCE').within(() => {
      cy.contains('NSE');
      cy.contains('EQUITY');
      cy.contains('0.05');
      cy.contains('1');
      cy.contains('INR');
    });

    cy.contains('[data-cy="broker-instrument-row"]', 'CRUDEOIL').within(() => {
      cy.contains('MCX');
      cy.contains('COMMODITY');
      cy.contains('1.00');
      cy.contains('10');
    });

    cy.then(() => {
      const loginToReadyMs = Date.now() - loginToReadyStart;
      expect(loginToReadyMs, 'broker desk ready under 60 seconds').to.be.lessThan(60_000);

      const metric = {
        measuredAt: new Date().toISOString(),
        loginToReadyMs,
        brokerId,
        instrumentCount,
      };

      cy.writeFile('target/cypress/broker/broker-seed-metrics.json', metric);
      cy.readFile('target/cypress/broker/broker-seed-metrics.json').its('loginToReadyMs').should('eq', metric.loginToReadyMs);
    });
  });
});
