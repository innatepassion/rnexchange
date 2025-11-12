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

describe('RiskAlert e2e test', () => {
  const riskAlertPageUrl = '/risk-alert';
  const riskAlertPageUrlPattern = new RegExp('/risk-alert(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const riskAlertSample = { alertType: 'MARGIN_BREACH' };

  let riskAlert;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/risk-alerts+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/risk-alerts').as('postEntityRequest');
    cy.intercept('DELETE', '/api/risk-alerts/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (riskAlert) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/risk-alerts/${riskAlert.id}`,
      }).then(() => {
        riskAlert = undefined;
      });
    }
  });

  it('RiskAlerts menu should load RiskAlerts page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('risk-alert');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('RiskAlert').should('exist');
    cy.url().should('match', riskAlertPageUrlPattern);
  });

  describe('RiskAlert page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(riskAlertPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create RiskAlert page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/risk-alert/new$'));
        cy.getEntityCreateUpdateHeading('RiskAlert');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', riskAlertPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/risk-alerts',
          body: riskAlertSample,
        }).then(({ body }) => {
          riskAlert = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/risk-alerts+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/risk-alerts?page=0&size=20>; rel="last",<http://localhost/api/risk-alerts?page=0&size=20>; rel="first"',
              },
              body: [riskAlert],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(riskAlertPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details RiskAlert page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('riskAlert');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', riskAlertPageUrlPattern);
      });

      it('edit button click should load edit RiskAlert page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('RiskAlert');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', riskAlertPageUrlPattern);
      });

      it('edit button click should load edit RiskAlert page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('RiskAlert');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', riskAlertPageUrlPattern);
      });

      it('last delete button click should delete instance of RiskAlert', () => {
        cy.intercept('GET', '/api/risk-alerts/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('riskAlert').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', riskAlertPageUrlPattern);

        riskAlert = undefined;
      });
    });
  });

  describe('new RiskAlert page', () => {
    beforeEach(() => {
      cy.visit(`${riskAlertPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('RiskAlert');
    });

    it('should create an instance of RiskAlert', () => {
      cy.get(`[data-cy="alertType"]`).select('AUTO_SQOFF');

      cy.get(`[data-cy="description"]`).type('reproach hmph sweatshop');
      cy.get(`[data-cy="description"]`).should('have.value', 'reproach hmph sweatshop');

      cy.get(`[data-cy="createdAt"]`).type('2025-11-12T05:17');
      cy.get(`[data-cy="createdAt"]`).blur();
      cy.get(`[data-cy="createdAt"]`).should('have.value', '2025-11-12T05:17');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        riskAlert = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', riskAlertPageUrlPattern);
    });
  });
});
