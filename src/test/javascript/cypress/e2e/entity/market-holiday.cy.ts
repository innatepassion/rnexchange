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

describe('MarketHoliday e2e test', () => {
  const marketHolidayPageUrl = '/market-holiday';
  const marketHolidayPageUrlPattern = new RegExp('/market-holiday(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const marketHolidaySample = { tradeDate: '2025-11-12', isHoliday: false };

  let marketHoliday;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/market-holidays+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/market-holidays').as('postEntityRequest');
    cy.intercept('DELETE', '/api/market-holidays/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (marketHoliday) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/market-holidays/${marketHoliday.id}`,
      }).then(() => {
        marketHoliday = undefined;
      });
    }
  });

  it('MarketHolidays menu should load MarketHolidays page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('market-holiday');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('MarketHoliday').should('exist');
    cy.url().should('match', marketHolidayPageUrlPattern);
  });

  describe('MarketHoliday page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(marketHolidayPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create MarketHoliday page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/market-holiday/new$'));
        cy.getEntityCreateUpdateHeading('MarketHoliday');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', marketHolidayPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/market-holidays',
          body: marketHolidaySample,
        }).then(({ body }) => {
          marketHoliday = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/market-holidays+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/market-holidays?page=0&size=20>; rel="last",<http://localhost/api/market-holidays?page=0&size=20>; rel="first"',
              },
              body: [marketHoliday],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(marketHolidayPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details MarketHoliday page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('marketHoliday');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', marketHolidayPageUrlPattern);
      });

      it('edit button click should load edit MarketHoliday page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('MarketHoliday');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', marketHolidayPageUrlPattern);
      });

      it('edit button click should load edit MarketHoliday page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('MarketHoliday');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', marketHolidayPageUrlPattern);
      });

      it('last delete button click should delete instance of MarketHoliday', () => {
        cy.intercept('GET', '/api/market-holidays/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('marketHoliday').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', marketHolidayPageUrlPattern);

        marketHoliday = undefined;
      });
    });
  });

  describe('new MarketHoliday page', () => {
    beforeEach(() => {
      cy.visit(`${marketHolidayPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('MarketHoliday');
    });

    it('should create an instance of MarketHoliday', () => {
      cy.get(`[data-cy="tradeDate"]`).type('2025-11-12');
      cy.get(`[data-cy="tradeDate"]`).blur();
      cy.get(`[data-cy="tradeDate"]`).should('have.value', '2025-11-12');

      cy.get(`[data-cy="reason"]`).type('warlike');
      cy.get(`[data-cy="reason"]`).should('have.value', 'warlike');

      cy.get(`[data-cy="isHoliday"]`).should('not.be.checked');
      cy.get(`[data-cy="isHoliday"]`).click();
      cy.get(`[data-cy="isHoliday"]`).should('be.checked');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        marketHoliday = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', marketHolidayPageUrlPattern);
    });
  });
});
