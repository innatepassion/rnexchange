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

describe('Execution e2e test', () => {
  const executionPageUrl = '/execution';
  const executionPageUrlPattern = new RegExp('/execution(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const executionSample = { execTs: '2025-11-11T20:25:54.259Z', px: 15452.91, qty: 13352.37 };

  let execution;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/executions+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/executions').as('postEntityRequest');
    cy.intercept('DELETE', '/api/executions/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (execution) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/executions/${execution.id}`,
      }).then(() => {
        execution = undefined;
      });
    }
  });

  it('Executions menu should load Executions page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('execution');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Execution').should('exist');
    cy.url().should('match', executionPageUrlPattern);
  });

  describe('Execution page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(executionPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Execution page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/execution/new$'));
        cy.getEntityCreateUpdateHeading('Execution');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', executionPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/executions',
          body: executionSample,
        }).then(({ body }) => {
          execution = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/executions+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/executions?page=0&size=20>; rel="last",<http://localhost/api/executions?page=0&size=20>; rel="first"',
              },
              body: [execution],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(executionPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Execution page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('execution');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', executionPageUrlPattern);
      });

      it('edit button click should load edit Execution page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Execution');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', executionPageUrlPattern);
      });

      it('edit button click should load edit Execution page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Execution');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', executionPageUrlPattern);
      });

      it('last delete button click should delete instance of Execution', () => {
        cy.intercept('GET', '/api/executions/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('execution').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', executionPageUrlPattern);

        execution = undefined;
      });
    });
  });

  describe('new Execution page', () => {
    beforeEach(() => {
      cy.visit(`${executionPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Execution');
    });

    it('should create an instance of Execution', () => {
      cy.get(`[data-cy="execTs"]`).type('2025-11-11T13:18');
      cy.get(`[data-cy="execTs"]`).blur();
      cy.get(`[data-cy="execTs"]`).should('have.value', '2025-11-11T13:18');

      cy.get(`[data-cy="px"]`).type('14778.07');
      cy.get(`[data-cy="px"]`).should('have.value', '14778.07');

      cy.get(`[data-cy="qty"]`).type('13667.43');
      cy.get(`[data-cy="qty"]`).should('have.value', '13667.43');

      cy.get(`[data-cy="liquidity"]`).type('who proliferate');
      cy.get(`[data-cy="liquidity"]`).should('have.value', 'who proliferate');

      cy.get(`[data-cy="fee"]`).type('30530.31');
      cy.get(`[data-cy="fee"]`).should('have.value', '30530.31');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        execution = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', executionPageUrlPattern);
    });
  });
});
