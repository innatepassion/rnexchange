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

describe('Position e2e test', () => {
  const positionPageUrl = '/position';
  const positionPageUrlPattern = new RegExp('/position(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const positionSample = { qty: 28768.07, avgCost: 24465.35 };

  let position;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/positions+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/positions').as('postEntityRequest');
    cy.intercept('DELETE', '/api/positions/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (position) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/positions/${position.id}`,
      }).then(() => {
        position = undefined;
      });
    }
  });

  it('Positions menu should load Positions page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('position');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Position').should('exist');
    cy.url().should('match', positionPageUrlPattern);
  });

  describe('Position page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(positionPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Position page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/position/new$'));
        cy.getEntityCreateUpdateHeading('Position');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', positionPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/positions',
          body: positionSample,
        }).then(({ body }) => {
          position = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/positions+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/positions?page=0&size=20>; rel="last",<http://localhost/api/positions?page=0&size=20>; rel="first"',
              },
              body: [position],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(positionPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Position page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('position');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', positionPageUrlPattern);
      });

      it('edit button click should load edit Position page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Position');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', positionPageUrlPattern);
      });

      it('edit button click should load edit Position page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Position');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', positionPageUrlPattern);
      });

      it('last delete button click should delete instance of Position', () => {
        cy.intercept('GET', '/api/positions/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('position').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', positionPageUrlPattern);

        position = undefined;
      });
    });
  });

  describe('new Position page', () => {
    beforeEach(() => {
      cy.visit(`${positionPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Position');
    });

    it('should create an instance of Position', () => {
      cy.get(`[data-cy="qty"]`).type('6558.93');
      cy.get(`[data-cy="qty"]`).should('have.value', '6558.93');

      cy.get(`[data-cy="avgCost"]`).type('31641.8');
      cy.get(`[data-cy="avgCost"]`).should('have.value', '31641.8');

      cy.get(`[data-cy="lastPx"]`).type('10923.58');
      cy.get(`[data-cy="lastPx"]`).should('have.value', '10923.58');

      cy.get(`[data-cy="unrealizedPnl"]`).type('27534.82');
      cy.get(`[data-cy="unrealizedPnl"]`).should('have.value', '27534.82');

      cy.get(`[data-cy="realizedPnl"]`).type('2952.88');
      cy.get(`[data-cy="realizedPnl"]`).should('have.value', '2952.88');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        position = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', positionPageUrlPattern);
    });
  });
});
