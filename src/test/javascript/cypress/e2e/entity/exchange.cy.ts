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

describe('Exchange e2e test', () => {
  const exchangePageUrl = '/exchange';
  const exchangePageUrlPattern = new RegExp('/exchange(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const exchangeSample = { code: 'until buzzing', name: 'starboard', timezone: 'owlishly now', status: 'ACTIVE' };

  let exchange;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/exchanges+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/exchanges').as('postEntityRequest');
    cy.intercept('DELETE', '/api/exchanges/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (exchange) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/exchanges/${exchange.id}`,
      }).then(() => {
        exchange = undefined;
      });
    }
  });

  it('Exchanges menu should load Exchanges page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('exchange');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Exchange').should('exist');
    cy.url().should('match', exchangePageUrlPattern);
  });

  describe('Exchange page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(exchangePageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Exchange page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/exchange/new$'));
        cy.getEntityCreateUpdateHeading('Exchange');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', exchangePageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/exchanges',
          body: exchangeSample,
        }).then(({ body }) => {
          exchange = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/exchanges+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/exchanges?page=0&size=20>; rel="last",<http://localhost/api/exchanges?page=0&size=20>; rel="first"',
              },
              body: [exchange],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(exchangePageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Exchange page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('exchange');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', exchangePageUrlPattern);
      });

      it('edit button click should load edit Exchange page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Exchange');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', exchangePageUrlPattern);
      });

      it('edit button click should load edit Exchange page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Exchange');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', exchangePageUrlPattern);
      });

      it('last delete button click should delete instance of Exchange', () => {
        cy.intercept('GET', '/api/exchanges/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('exchange').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', exchangePageUrlPattern);

        exchange = undefined;
      });
    });
  });

  describe('new Exchange page', () => {
    beforeEach(() => {
      cy.visit(`${exchangePageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Exchange');
    });

    it('should create an instance of Exchange', () => {
      cy.get(`[data-cy="code"]`).type('braid');
      cy.get(`[data-cy="code"]`).should('have.value', 'braid');

      cy.get(`[data-cy="name"]`).type('astride fooey fondly');
      cy.get(`[data-cy="name"]`).should('have.value', 'astride fooey fondly');

      cy.get(`[data-cy="timezone"]`).type('pfft');
      cy.get(`[data-cy="timezone"]`).should('have.value', 'pfft');

      cy.get(`[data-cy="status"]`).select('ACTIVE');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        exchange = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', exchangePageUrlPattern);
    });
  });
});
