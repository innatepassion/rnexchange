import {
  entityConfirmDeleteButtonSelector,
  entityCreateButtonSelector,
  entityCreateCancelButtonSelector,
  entityCreateSaveButtonSelector,
  entityDeleteButtonSelector,
  entityDetailsBackButtonSelector,
  entityDetailsButtonSelector,
  entityEditButtonSelector,
  entityTableSelector,
} from '../../support/entity';

describe('TradingAccount e2e test', () => {
  const tradingAccountPageUrl = '/trading-account';
  const tradingAccountPageUrlPattern = new RegExp('/trading-account(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const tradingAccountSample = { type: 'CASH', baseCcy: 'INR', balance: 28295.19, status: 'INACTIVE' };

  let tradingAccount;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/trading-accounts+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/trading-accounts').as('postEntityRequest');
    cy.intercept('DELETE', '/api/trading-accounts/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (tradingAccount) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/trading-accounts/${tradingAccount.id}`,
      }).then(() => {
        tradingAccount = undefined;
      });
    }
  });

  it('TradingAccounts menu should load TradingAccounts page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('trading-account');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('TradingAccount').should('exist');
    cy.url().should('match', tradingAccountPageUrlPattern);
  });

  describe('TradingAccount page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(tradingAccountPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create TradingAccount page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/trading-account/new$'));
        cy.getEntityCreateUpdateHeading('TradingAccount');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', tradingAccountPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/trading-accounts',
          body: tradingAccountSample,
        }).then(({ body }) => {
          tradingAccount = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/trading-accounts+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/trading-accounts?page=0&size=20>; rel="last",<http://localhost/api/trading-accounts?page=0&size=20>; rel="first"',
              },
              body: [tradingAccount],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(tradingAccountPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details TradingAccount page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('tradingAccount');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', tradingAccountPageUrlPattern);
      });

      it('edit button click should load edit TradingAccount page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('TradingAccount');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', tradingAccountPageUrlPattern);
      });

      it('edit button click should load edit TradingAccount page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('TradingAccount');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', tradingAccountPageUrlPattern);
      });

      it('last delete button click should delete instance of TradingAccount', () => {
        cy.intercept('GET', '/api/trading-accounts/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('tradingAccount').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', tradingAccountPageUrlPattern);

        tradingAccount = undefined;
      });
    });
  });

  describe('new TradingAccount page', () => {
    beforeEach(() => {
      cy.visit(`${tradingAccountPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('TradingAccount');
    });

    it('should create an instance of TradingAccount', () => {
      cy.get(`[data-cy="type"]`).select('MARGIN');

      cy.get(`[data-cy="baseCcy"]`).select('USD');

      cy.get(`[data-cy="balance"]`).type('27238.43');
      cy.get(`[data-cy="balance"]`).should('have.value', '27238.43');

      cy.get(`[data-cy="status"]`).select('SUSPENDED');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        tradingAccount = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', tradingAccountPageUrlPattern);
    });
  });
});
