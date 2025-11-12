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

describe('ExchangeOperator e2e test', () => {
  const exchangeOperatorPageUrl = '/exchange-operator';
  const exchangeOperatorPageUrlPattern = new RegExp('/exchange-operator(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const exchangeOperatorSample = { name: 'acceptable wilderness loudly' };

  let exchangeOperator;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/exchange-operators+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/exchange-operators').as('postEntityRequest');
    cy.intercept('DELETE', '/api/exchange-operators/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (exchangeOperator) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/exchange-operators/${exchangeOperator.id}`,
      }).then(() => {
        exchangeOperator = undefined;
      });
    }
  });

  it('ExchangeOperators menu should load ExchangeOperators page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('exchange-operator');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('ExchangeOperator').should('exist');
    cy.url().should('match', exchangeOperatorPageUrlPattern);
  });

  describe('ExchangeOperator page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(exchangeOperatorPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create ExchangeOperator page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/exchange-operator/new$'));
        cy.getEntityCreateUpdateHeading('ExchangeOperator');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', exchangeOperatorPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/exchange-operators',
          body: exchangeOperatorSample,
        }).then(({ body }) => {
          exchangeOperator = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/exchange-operators+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/exchange-operators?page=0&size=20>; rel="last",<http://localhost/api/exchange-operators?page=0&size=20>; rel="first"',
              },
              body: [exchangeOperator],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(exchangeOperatorPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details ExchangeOperator page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('exchangeOperator');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', exchangeOperatorPageUrlPattern);
      });

      it('edit button click should load edit ExchangeOperator page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('ExchangeOperator');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', exchangeOperatorPageUrlPattern);
      });

      it('edit button click should load edit ExchangeOperator page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('ExchangeOperator');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', exchangeOperatorPageUrlPattern);
      });

      it('last delete button click should delete instance of ExchangeOperator', () => {
        cy.intercept('GET', '/api/exchange-operators/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('exchangeOperator').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', exchangeOperatorPageUrlPattern);

        exchangeOperator = undefined;
      });
    });
  });

  describe('new ExchangeOperator page', () => {
    beforeEach(() => {
      cy.visit(`${exchangeOperatorPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('ExchangeOperator');
    });

    it('should create an instance of ExchangeOperator', () => {
      cy.get(`[data-cy="name"]`).type('fairly');
      cy.get(`[data-cy="name"]`).should('have.value', 'fairly');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        exchangeOperator = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', exchangeOperatorPageUrlPattern);
    });
  });
});
