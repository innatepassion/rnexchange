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

describe('Broker e2e test', () => {
  const brokerPageUrl = '/broker';
  const brokerPageUrlPattern = new RegExp('/broker(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const brokerSample = { code: 'woot saloon', name: 'pause', status: 'surprisingly whose' };

  let broker;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/brokers+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/brokers').as('postEntityRequest');
    cy.intercept('DELETE', '/api/brokers/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (broker) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/brokers/${broker.id}`,
      }).then(() => {
        broker = undefined;
      });
    }
  });

  it('Brokers menu should load Brokers page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('broker');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Broker').should('exist');
    cy.url().should('match', brokerPageUrlPattern);
  });

  describe('Broker page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(brokerPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Broker page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/broker/new$'));
        cy.getEntityCreateUpdateHeading('Broker');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', brokerPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/brokers',
          body: brokerSample,
        }).then(({ body }) => {
          broker = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/brokers+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/brokers?page=0&size=20>; rel="last",<http://localhost/api/brokers?page=0&size=20>; rel="first"',
              },
              body: [broker],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(brokerPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Broker page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('broker');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', brokerPageUrlPattern);
      });

      it('edit button click should load edit Broker page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Broker');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', brokerPageUrlPattern);
      });

      it('edit button click should load edit Broker page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Broker');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', brokerPageUrlPattern);
      });

      it('last delete button click should delete instance of Broker', () => {
        cy.intercept('GET', '/api/brokers/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('broker').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', brokerPageUrlPattern);

        broker = undefined;
      });
    });
  });

  describe('new Broker page', () => {
    beforeEach(() => {
      cy.visit(`${brokerPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Broker');
    });

    it('should create an instance of Broker', () => {
      cy.get(`[data-cy="code"]`).type('gee outside yuck');
      cy.get(`[data-cy="code"]`).should('have.value', 'gee outside yuck');

      cy.get(`[data-cy="name"]`).type('good-natured bitterly');
      cy.get(`[data-cy="name"]`).should('have.value', 'good-natured bitterly');

      cy.get(`[data-cy="status"]`).type('sweet');
      cy.get(`[data-cy="status"]`).should('have.value', 'sweet');

      cy.get(`[data-cy="createdDate"]`).type('2025-11-11T22:22');
      cy.get(`[data-cy="createdDate"]`).blur();
      cy.get(`[data-cy="createdDate"]`).should('have.value', '2025-11-11T22:22');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        broker = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', brokerPageUrlPattern);
    });
  });
});
