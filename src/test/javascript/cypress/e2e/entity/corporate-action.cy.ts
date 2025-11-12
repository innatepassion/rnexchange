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

describe('CorporateAction e2e test', () => {
  const corporateActionPageUrl = '/corporate-action';
  const corporateActionPageUrlPattern = new RegExp('/corporate-action(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const corporateActionSample = { type: 'SPLIT', instrumentSymbol: 'afterwards over taxicab', exDate: '2025-11-11' };

  let corporateAction;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/corporate-actions+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/corporate-actions').as('postEntityRequest');
    cy.intercept('DELETE', '/api/corporate-actions/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (corporateAction) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/corporate-actions/${corporateAction.id}`,
      }).then(() => {
        corporateAction = undefined;
      });
    }
  });

  it('CorporateActions menu should load CorporateActions page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('corporate-action');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('CorporateAction').should('exist');
    cy.url().should('match', corporateActionPageUrlPattern);
  });

  describe('CorporateAction page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(corporateActionPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create CorporateAction page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/corporate-action/new$'));
        cy.getEntityCreateUpdateHeading('CorporateAction');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', corporateActionPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/corporate-actions',
          body: corporateActionSample,
        }).then(({ body }) => {
          corporateAction = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/corporate-actions+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/corporate-actions?page=0&size=20>; rel="last",<http://localhost/api/corporate-actions?page=0&size=20>; rel="first"',
              },
              body: [corporateAction],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(corporateActionPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details CorporateAction page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('corporateAction');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', corporateActionPageUrlPattern);
      });

      it('edit button click should load edit CorporateAction page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('CorporateAction');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', corporateActionPageUrlPattern);
      });

      it('edit button click should load edit CorporateAction page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('CorporateAction');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', corporateActionPageUrlPattern);
      });

      it('last delete button click should delete instance of CorporateAction', () => {
        cy.intercept('GET', '/api/corporate-actions/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('corporateAction').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', corporateActionPageUrlPattern);

        corporateAction = undefined;
      });
    });
  });

  describe('new CorporateAction page', () => {
    beforeEach(() => {
      cy.visit(`${corporateActionPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('CorporateAction');
    });

    it('should create an instance of CorporateAction', () => {
      cy.get(`[data-cy="type"]`).select('SPLIT');

      cy.get(`[data-cy="instrumentSymbol"]`).type('secret');
      cy.get(`[data-cy="instrumentSymbol"]`).should('have.value', 'secret');

      cy.get(`[data-cy="exDate"]`).type('2025-11-12');
      cy.get(`[data-cy="exDate"]`).blur();
      cy.get(`[data-cy="exDate"]`).should('have.value', '2025-11-12');

      cy.get(`[data-cy="payDate"]`).type('2025-11-12');
      cy.get(`[data-cy="payDate"]`).blur();
      cy.get(`[data-cy="payDate"]`).should('have.value', '2025-11-12');

      cy.get(`[data-cy="ratio"]`).type('15700.15');
      cy.get(`[data-cy="ratio"]`).should('have.value', '15700.15');

      cy.get(`[data-cy="cashAmount"]`).type('24829.81');
      cy.get(`[data-cy="cashAmount"]`).should('have.value', '24829.81');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        corporateAction = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', corporateActionPageUrlPattern);
    });
  });
});
