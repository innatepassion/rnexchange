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

describe('Order e2e test', () => {
  const orderPageUrl = '/order';
  const orderPageUrlPattern = new RegExp('/order(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const orderSample = { side: 'BUY', type: 'STOP_LIMIT', qty: 5780.81, tif: 'IOC', status: 'FILLED', venue: 'past ah' };

  let order;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/orders+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/orders').as('postEntityRequest');
    cy.intercept('DELETE', '/api/orders/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (order) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/orders/${order.id}`,
      }).then(() => {
        order = undefined;
      });
    }
  });

  it('Orders menu should load Orders page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('order');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Order').should('exist');
    cy.url().should('match', orderPageUrlPattern);
  });

  describe('Order page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(orderPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Order page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/order/new$'));
        cy.getEntityCreateUpdateHeading('Order');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', orderPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/orders',
          body: orderSample,
        }).then(({ body }) => {
          order = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/orders+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/orders?page=0&size=20>; rel="last",<http://localhost/api/orders?page=0&size=20>; rel="first"',
              },
              body: [order],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(orderPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Order page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('order');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', orderPageUrlPattern);
      });

      it('edit button click should load edit Order page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Order');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', orderPageUrlPattern);
      });

      it('edit button click should load edit Order page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Order');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', orderPageUrlPattern);
      });

      it('last delete button click should delete instance of Order', () => {
        cy.intercept('GET', '/api/orders/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('order').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', orderPageUrlPattern);

        order = undefined;
      });
    });
  });

  describe('new Order page', () => {
    beforeEach(() => {
      cy.visit(`${orderPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Order');
    });

    it('should create an instance of Order', () => {
      cy.get(`[data-cy="side"]`).select('BUY');

      cy.get(`[data-cy="type"]`).select('STOP_LIMIT');

      cy.get(`[data-cy="qty"]`).type('1418.56');
      cy.get(`[data-cy="qty"]`).should('have.value', '1418.56');

      cy.get(`[data-cy="limitPx"]`).type('32432.96');
      cy.get(`[data-cy="limitPx"]`).should('have.value', '32432.96');

      cy.get(`[data-cy="stopPx"]`).type('15303.15');
      cy.get(`[data-cy="stopPx"]`).should('have.value', '15303.15');

      cy.get(`[data-cy="tif"]`).select('DAY');

      cy.get(`[data-cy="status"]`).select('ACCEPTED');

      cy.get(`[data-cy="venue"]`).type('um woot');
      cy.get(`[data-cy="venue"]`).should('have.value', 'um woot');

      cy.get(`[data-cy="createdAt"]`).type('2025-11-12T08:14');
      cy.get(`[data-cy="createdAt"]`).blur();
      cy.get(`[data-cy="createdAt"]`).should('have.value', '2025-11-12T08:14');

      cy.get(`[data-cy="updatedAt"]`).type('2025-11-11T20:23');
      cy.get(`[data-cy="updatedAt"]`).blur();
      cy.get(`[data-cy="updatedAt"]`).should('have.value', '2025-11-11T20:23');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        order = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', orderPageUrlPattern);
    });
  });
});
