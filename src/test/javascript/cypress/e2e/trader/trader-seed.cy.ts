describe('Trader baseline order simulation', () => {
  const username = Cypress.env('TRADER_USERNAME') ?? 'trader-one';
  const password = Cypress.env('TRADER_PASSWORD') ?? 'password';

  let tradingAccountId: number;
  let instrumentId: number;
  let instrumentSymbol = 'RELIANCE';

  before(() => {
    cy.login(username, password);
    cy.authenticatedRequest({
      method: 'GET',
      url: '/api/trading-accounts?sort=id,asc',
    }).then(({ body }) => {
      const tradingAccount = body.find(
        (candidate: { broker?: { code?: string }; trader?: { email?: string } }) =>
          candidate?.broker?.code === 'RN_DEMO' && candidate?.trader?.email === 'trader.one@rnexchange.test',
      );
      if (!tradingAccount?.id) {
        throw new Error('Expected seeded trading account for trader-one to exist');
      }
      tradingAccountId = tradingAccount.id;
    });

    cy.authenticatedRequest({
      method: 'GET',
      url: '/api/instruments?sort=id,asc',
    }).then(({ body }) => {
      const instrument = body.find(
        (candidate: { symbol?: string; exchange?: { code?: string } }) =>
          candidate?.symbol === instrumentSymbol && candidate?.exchange?.code === 'NSE',
      );
      if (!instrument?.id) {
        throw new Error(`Expected seeded instrument ${instrumentSymbol} on NSE to exist`);
      }
      instrumentId = instrument.id;
    });
  });

  beforeEach(() => {
    cy.login(username, password);
  });

  it('submits three consecutive trader orders with deterministic defaults', () => {
    if (!tradingAccountId || !instrumentId) {
      throw new Error('Seeded trading account or instrument not initialised');
    }

    const submitOrder = (runIndex: number) => {
      cy.intercept('POST', '/api/orders').as(`createOrder${runIndex}`);

      cy.visit('/order/new');

      cy.get('[data-cy="tradingAccount"]').should('have.value', `${tradingAccountId}`);
      cy.get('[data-cy="instrument"]').should('have.value', `${instrumentId}`);

      cy.get('[data-cy="side"]').should('have.value', 'BUY');
      cy.get('[data-cy="type"]').should('have.value', 'MARKET');
      cy.get('[data-cy="tif"]').should('have.value', 'DAY');
      cy.get('[data-cy="status"]').should('have.value', 'NEW');
      cy.get('[data-cy="venue"]').should('have.value', 'NSE');

      cy.get('[data-cy="qty"]').clear().type('10');
      cy.get('[data-cy="limitPx"]').clear().type('2200');

      cy.get('[data-cy="entityCreateSaveButton"]').click();

      cy.wait(`@createOrder${runIndex}`).then(interception => {
        expect(interception.response?.statusCode).to.eq(201);
        expect(interception.response?.body?.status).to.eq('ACCEPTED');
        expect(interception.response?.body?.instrument?.symbol).to.eq(instrumentSymbol);
        expect(interception.response?.body?.tradingAccount?.id).to.eq(tradingAccountId);

        const artifact = {
          runIndex,
          orderId: interception.response?.body?.id,
          submittedAt: new Date().toISOString(),
          status: interception.response?.body?.status,
          qty: interception.response?.body?.qty,
          limitPx: interception.response?.body?.limitPx,
        };
        cy.writeFile(`target/cypress/trader/trader-seed-run-${runIndex}.json`, artifact);
      });
    };

    submitOrder(1);
    submitOrder(2);
    submitOrder(3);
  });
});
