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

describe('Lot e2e test', () => {
  const lotPageUrl = '/lot';
  const lotPageUrlPattern = new RegExp('/lot(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const lotSample = { openTs: '2025-11-11T14:35:02.124Z', openPx: 9339.73, qtyOpen: 13968.71, qtyClosed: 22000.45 };

  let lot;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/lots+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/lots').as('postEntityRequest');
    cy.intercept('DELETE', '/api/lots/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (lot) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/lots/${lot.id}`,
      }).then(() => {
        lot = undefined;
      });
    }
  });

  it('Lots menu should load Lots page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('lot');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Lot').should('exist');
    cy.url().should('match', lotPageUrlPattern);
  });

  describe('Lot page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(lotPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Lot page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/lot/new$'));
        cy.getEntityCreateUpdateHeading('Lot');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', lotPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/lots',
          body: lotSample,
        }).then(({ body }) => {
          lot = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/lots+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/lots?page=0&size=20>; rel="last",<http://localhost/api/lots?page=0&size=20>; rel="first"',
              },
              body: [lot],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(lotPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Lot page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('lot');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', lotPageUrlPattern);
      });

      it('edit button click should load edit Lot page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Lot');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', lotPageUrlPattern);
      });

      it('edit button click should load edit Lot page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Lot');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', lotPageUrlPattern);
      });

      it('last delete button click should delete instance of Lot', () => {
        cy.intercept('GET', '/api/lots/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('lot').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', lotPageUrlPattern);

        lot = undefined;
      });
    });
  });

  describe('new Lot page', () => {
    beforeEach(() => {
      cy.visit(`${lotPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Lot');
    });

    it('should create an instance of Lot', () => {
      cy.get(`[data-cy="openTs"]`).type('2025-11-12T03:57');
      cy.get(`[data-cy="openTs"]`).blur();
      cy.get(`[data-cy="openTs"]`).should('have.value', '2025-11-12T03:57');

      cy.get(`[data-cy="openPx"]`).type('991.92');
      cy.get(`[data-cy="openPx"]`).should('have.value', '991.92');

      cy.get(`[data-cy="qtyOpen"]`).type('26337.91');
      cy.get(`[data-cy="qtyOpen"]`).should('have.value', '26337.91');

      cy.get(`[data-cy="qtyClosed"]`).type('22976.86');
      cy.get(`[data-cy="qtyClosed"]`).should('have.value', '22976.86');

      cy.get(`[data-cy="method"]`).type('duh');
      cy.get(`[data-cy="method"]`).should('have.value', 'duh');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        lot = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', lotPageUrlPattern);
    });
  });
});
