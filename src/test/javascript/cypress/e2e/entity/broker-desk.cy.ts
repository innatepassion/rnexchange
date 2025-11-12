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

describe('BrokerDesk e2e test', () => {
  const brokerDeskPageUrl = '/broker-desk';
  const brokerDeskPageUrlPattern = new RegExp('/broker-desk(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const brokerDeskSample = { name: 'glum besides sharply' };

  let brokerDesk;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/broker-desks+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/broker-desks').as('postEntityRequest');
    cy.intercept('DELETE', '/api/broker-desks/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (brokerDesk) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/broker-desks/${brokerDesk.id}`,
      }).then(() => {
        brokerDesk = undefined;
      });
    }
  });

  it('BrokerDesks menu should load BrokerDesks page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('broker-desk');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('BrokerDesk').should('exist');
    cy.url().should('match', brokerDeskPageUrlPattern);
  });

  describe('BrokerDesk page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(brokerDeskPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create BrokerDesk page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/broker-desk/new$'));
        cy.getEntityCreateUpdateHeading('BrokerDesk');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', brokerDeskPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/broker-desks',
          body: brokerDeskSample,
        }).then(({ body }) => {
          brokerDesk = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/broker-desks+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/broker-desks?page=0&size=20>; rel="last",<http://localhost/api/broker-desks?page=0&size=20>; rel="first"',
              },
              body: [brokerDesk],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(brokerDeskPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details BrokerDesk page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('brokerDesk');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', brokerDeskPageUrlPattern);
      });

      it('edit button click should load edit BrokerDesk page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('BrokerDesk');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', brokerDeskPageUrlPattern);
      });

      it('edit button click should load edit BrokerDesk page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('BrokerDesk');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', brokerDeskPageUrlPattern);
      });

      it('last delete button click should delete instance of BrokerDesk', () => {
        cy.intercept('GET', '/api/broker-desks/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('brokerDesk').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', brokerDeskPageUrlPattern);

        brokerDesk = undefined;
      });
    });
  });

  describe('new BrokerDesk page', () => {
    beforeEach(() => {
      cy.visit(`${brokerDeskPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('BrokerDesk');
    });

    it('should create an instance of BrokerDesk', () => {
      cy.get(`[data-cy="name"]`).type('hourly considering frantically');
      cy.get(`[data-cy="name"]`).should('have.value', 'hourly considering frantically');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        brokerDesk = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', brokerDeskPageUrlPattern);
    });
  });
});
