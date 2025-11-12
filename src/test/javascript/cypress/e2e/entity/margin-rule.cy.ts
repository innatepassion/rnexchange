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

describe('MarginRule e2e test', () => {
  const marginRulePageUrl = '/margin-rule';
  const marginRulePageUrlPattern = new RegExp('/margin-rule(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const marginRuleSample = { scope: 'gah ouch cross' };

  let marginRule;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/margin-rules+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/margin-rules').as('postEntityRequest');
    cy.intercept('DELETE', '/api/margin-rules/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (marginRule) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/margin-rules/${marginRule.id}`,
      }).then(() => {
        marginRule = undefined;
      });
    }
  });

  it('MarginRules menu should load MarginRules page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('margin-rule');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('MarginRule').should('exist');
    cy.url().should('match', marginRulePageUrlPattern);
  });

  describe('MarginRule page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(marginRulePageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create MarginRule page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/margin-rule/new$'));
        cy.getEntityCreateUpdateHeading('MarginRule');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', marginRulePageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/margin-rules',
          body: marginRuleSample,
        }).then(({ body }) => {
          marginRule = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/margin-rules+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/margin-rules?page=0&size=20>; rel="last",<http://localhost/api/margin-rules?page=0&size=20>; rel="first"',
              },
              body: [marginRule],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(marginRulePageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details MarginRule page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('marginRule');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', marginRulePageUrlPattern);
      });

      it('edit button click should load edit MarginRule page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('MarginRule');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', marginRulePageUrlPattern);
      });

      it('edit button click should load edit MarginRule page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('MarginRule');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', marginRulePageUrlPattern);
      });

      it('last delete button click should delete instance of MarginRule', () => {
        cy.intercept('GET', '/api/margin-rules/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('marginRule').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', marginRulePageUrlPattern);

        marginRule = undefined;
      });
    });
  });

  describe('new MarginRule page', () => {
    beforeEach(() => {
      cy.visit(`${marginRulePageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('MarginRule');
    });

    it('should create an instance of MarginRule', () => {
      cy.get(`[data-cy="scope"]`).type('er');
      cy.get(`[data-cy="scope"]`).should('have.value', 'er');

      cy.get(`[data-cy="initialPct"]`).type('24203.19');
      cy.get(`[data-cy="initialPct"]`).should('have.value', '24203.19');

      cy.get(`[data-cy="maintPct"]`).type('12440.3');
      cy.get(`[data-cy="maintPct"]`).should('have.value', '12440.3');

      cy.get(`[data-cy="spanJson"]`).type('../fake-data/blob/hipster.txt');
      cy.get(`[data-cy="spanJson"]`).invoke('val').should('match', new RegExp('../fake-data/blob/hipster.txt'));

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        marginRule = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', marginRulePageUrlPattern);
    });
  });
});
