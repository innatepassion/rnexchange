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

describe('SettlementBatch e2e test', () => {
  const settlementBatchPageUrl = '/settlement-batch';
  const settlementBatchPageUrlPattern = new RegExp('/settlement-batch(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const settlementBatchSample = { refDate: '2025-11-11', kind: 'VARIATION', status: 'REVERSED' };

  let settlementBatch;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/settlement-batches+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/settlement-batches').as('postEntityRequest');
    cy.intercept('DELETE', '/api/settlement-batches/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (settlementBatch) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/settlement-batches/${settlementBatch.id}`,
      }).then(() => {
        settlementBatch = undefined;
      });
    }
  });

  it('SettlementBatches menu should load SettlementBatches page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('settlement-batch');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('SettlementBatch').should('exist');
    cy.url().should('match', settlementBatchPageUrlPattern);
  });

  describe('SettlementBatch page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(settlementBatchPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create SettlementBatch page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/settlement-batch/new$'));
        cy.getEntityCreateUpdateHeading('SettlementBatch');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', settlementBatchPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/settlement-batches',
          body: settlementBatchSample,
        }).then(({ body }) => {
          settlementBatch = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/settlement-batches+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/settlement-batches?page=0&size=20>; rel="last",<http://localhost/api/settlement-batches?page=0&size=20>; rel="first"',
              },
              body: [settlementBatch],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(settlementBatchPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details SettlementBatch page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('settlementBatch');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', settlementBatchPageUrlPattern);
      });

      it('edit button click should load edit SettlementBatch page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('SettlementBatch');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', settlementBatchPageUrlPattern);
      });

      it('edit button click should load edit SettlementBatch page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('SettlementBatch');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', settlementBatchPageUrlPattern);
      });

      it('last delete button click should delete instance of SettlementBatch', () => {
        cy.intercept('GET', '/api/settlement-batches/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('settlementBatch').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', settlementBatchPageUrlPattern);

        settlementBatch = undefined;
      });
    });
  });

  describe('new SettlementBatch page', () => {
    beforeEach(() => {
      cy.visit(`${settlementBatchPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('SettlementBatch');
    });

    it('should create an instance of SettlementBatch', () => {
      cy.get(`[data-cy="refDate"]`).type('2025-11-11');
      cy.get(`[data-cy="refDate"]`).blur();
      cy.get(`[data-cy="refDate"]`).should('have.value', '2025-11-11');

      cy.get(`[data-cy="kind"]`).select('EOD');

      cy.get(`[data-cy="status"]`).select('PROCESSED');

      cy.get(`[data-cy="remarks"]`).type('till');
      cy.get(`[data-cy="remarks"]`).should('have.value', 'till');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        settlementBatch = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', settlementBatchPageUrlPattern);
    });
  });
});
