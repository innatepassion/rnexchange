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

describe('ExchangeIntegration e2e test', () => {
  const exchangeIntegrationPageUrl = '/exchange-integration';
  const exchangeIntegrationPageUrlPattern = new RegExp('/exchange-integration(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const exchangeIntegrationSample = { provider: 'alongside', status: 'DISABLED' };

  let exchangeIntegration;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/exchange-integrations+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/exchange-integrations').as('postEntityRequest');
    cy.intercept('DELETE', '/api/exchange-integrations/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (exchangeIntegration) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/exchange-integrations/${exchangeIntegration.id}`,
      }).then(() => {
        exchangeIntegration = undefined;
      });
    }
  });

  it('ExchangeIntegrations menu should load ExchangeIntegrations page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('exchange-integration');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('ExchangeIntegration').should('exist');
    cy.url().should('match', exchangeIntegrationPageUrlPattern);
  });

  describe('ExchangeIntegration page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(exchangeIntegrationPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create ExchangeIntegration page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/exchange-integration/new$'));
        cy.getEntityCreateUpdateHeading('ExchangeIntegration');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', exchangeIntegrationPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/exchange-integrations',
          body: exchangeIntegrationSample,
        }).then(({ body }) => {
          exchangeIntegration = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/exchange-integrations+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/exchange-integrations?page=0&size=20>; rel="last",<http://localhost/api/exchange-integrations?page=0&size=20>; rel="first"',
              },
              body: [exchangeIntegration],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(exchangeIntegrationPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details ExchangeIntegration page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('exchangeIntegration');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', exchangeIntegrationPageUrlPattern);
      });

      it('edit button click should load edit ExchangeIntegration page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('ExchangeIntegration');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', exchangeIntegrationPageUrlPattern);
      });

      it('edit button click should load edit ExchangeIntegration page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('ExchangeIntegration');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', exchangeIntegrationPageUrlPattern);
      });

      it('last delete button click should delete instance of ExchangeIntegration', () => {
        cy.intercept('GET', '/api/exchange-integrations/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('exchangeIntegration').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', exchangeIntegrationPageUrlPattern);

        exchangeIntegration = undefined;
      });
    });
  });

  describe('new ExchangeIntegration page', () => {
    beforeEach(() => {
      cy.visit(`${exchangeIntegrationPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('ExchangeIntegration');
    });

    it('should create an instance of ExchangeIntegration', () => {
      cy.get(`[data-cy="provider"]`).type('nor wherever yum');
      cy.get(`[data-cy="provider"]`).should('have.value', 'nor wherever yum');

      cy.get(`[data-cy="apiKey"]`).type('puritan insolence');
      cy.get(`[data-cy="apiKey"]`).should('have.value', 'puritan insolence');

      cy.get(`[data-cy="apiSecret"]`).type('excepting and');
      cy.get(`[data-cy="apiSecret"]`).should('have.value', 'excepting and');

      cy.get(`[data-cy="status"]`).select('DISABLED');

      cy.get(`[data-cy="lastHeartbeat"]`).type('2025-11-11T14:07');
      cy.get(`[data-cy="lastHeartbeat"]`).blur();
      cy.get(`[data-cy="lastHeartbeat"]`).should('have.value', '2025-11-11T14:07');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        exchangeIntegration = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', exchangeIntegrationPageUrlPattern);
    });
  });
});
