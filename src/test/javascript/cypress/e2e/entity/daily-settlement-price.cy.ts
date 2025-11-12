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

describe('DailySettlementPrice e2e test', () => {
  const dailySettlementPricePageUrl = '/daily-settlement-price';
  const dailySettlementPricePageUrlPattern = new RegExp('/daily-settlement-price(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const dailySettlementPriceSample = { refDate: '2025-11-12', instrumentSymbol: 'geez ready tut', settlePrice: 1810.48 };

  let dailySettlementPrice;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/daily-settlement-prices+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/daily-settlement-prices').as('postEntityRequest');
    cy.intercept('DELETE', '/api/daily-settlement-prices/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (dailySettlementPrice) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/daily-settlement-prices/${dailySettlementPrice.id}`,
      }).then(() => {
        dailySettlementPrice = undefined;
      });
    }
  });

  it('DailySettlementPrices menu should load DailySettlementPrices page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('daily-settlement-price');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('DailySettlementPrice').should('exist');
    cy.url().should('match', dailySettlementPricePageUrlPattern);
  });

  describe('DailySettlementPrice page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(dailySettlementPricePageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create DailySettlementPrice page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/daily-settlement-price/new$'));
        cy.getEntityCreateUpdateHeading('DailySettlementPrice');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', dailySettlementPricePageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/daily-settlement-prices',
          body: dailySettlementPriceSample,
        }).then(({ body }) => {
          dailySettlementPrice = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/daily-settlement-prices+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/daily-settlement-prices?page=0&size=20>; rel="last",<http://localhost/api/daily-settlement-prices?page=0&size=20>; rel="first"',
              },
              body: [dailySettlementPrice],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(dailySettlementPricePageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details DailySettlementPrice page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('dailySettlementPrice');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', dailySettlementPricePageUrlPattern);
      });

      it('edit button click should load edit DailySettlementPrice page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('DailySettlementPrice');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', dailySettlementPricePageUrlPattern);
      });

      it('edit button click should load edit DailySettlementPrice page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('DailySettlementPrice');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', dailySettlementPricePageUrlPattern);
      });

      it('last delete button click should delete instance of DailySettlementPrice', () => {
        cy.intercept('GET', '/api/daily-settlement-prices/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('dailySettlementPrice').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', dailySettlementPricePageUrlPattern);

        dailySettlementPrice = undefined;
      });
    });
  });

  describe('new DailySettlementPrice page', () => {
    beforeEach(() => {
      cy.visit(`${dailySettlementPricePageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('DailySettlementPrice');
    });

    it('should create an instance of DailySettlementPrice', () => {
      cy.get(`[data-cy="refDate"]`).type('2025-11-12');
      cy.get(`[data-cy="refDate"]`).blur();
      cy.get(`[data-cy="refDate"]`).should('have.value', '2025-11-12');

      cy.get(`[data-cy="instrumentSymbol"]`).type('ugh disappointment woeful');
      cy.get(`[data-cy="instrumentSymbol"]`).should('have.value', 'ugh disappointment woeful');

      cy.get(`[data-cy="settlePrice"]`).type('20729.76');
      cy.get(`[data-cy="settlePrice"]`).should('have.value', '20729.76');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        dailySettlementPrice = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', dailySettlementPricePageUrlPattern);
    });
  });
});
