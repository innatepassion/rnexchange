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

describe('Instrument e2e test', () => {
  const instrumentPageUrl = '/instrument';
  const instrumentPageUrlPattern = new RegExp('/instrument(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const instrumentSample = {
    symbol: 'scarcely',
    assetClass: 'OPTION',
    exchangeCode: 'whenever',
    tickSize: 19525.92,
    lotSize: 1109,
    currency: 'USD',
    status: 'foodstuffs what',
  };

  let instrument;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/instruments+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/instruments').as('postEntityRequest');
    cy.intercept('DELETE', '/api/instruments/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (instrument) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/instruments/${instrument.id}`,
      }).then(() => {
        instrument = undefined;
      });
    }
  });

  it('Instruments menu should load Instruments page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('instrument');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Instrument').should('exist');
    cy.url().should('match', instrumentPageUrlPattern);
  });

  describe('Instrument page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(instrumentPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Instrument page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/instrument/new$'));
        cy.getEntityCreateUpdateHeading('Instrument');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', instrumentPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/instruments',
          body: instrumentSample,
        }).then(({ body }) => {
          instrument = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/instruments+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/instruments?page=0&size=20>; rel="last",<http://localhost/api/instruments?page=0&size=20>; rel="first"',
              },
              body: [instrument],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(instrumentPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Instrument page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('instrument');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', instrumentPageUrlPattern);
      });

      it('edit button click should load edit Instrument page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Instrument');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', instrumentPageUrlPattern);
      });

      it('edit button click should load edit Instrument page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Instrument');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', instrumentPageUrlPattern);
      });

      it('last delete button click should delete instance of Instrument', () => {
        cy.intercept('GET', '/api/instruments/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('instrument').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', instrumentPageUrlPattern);

        instrument = undefined;
      });
    });
  });

  describe('new Instrument page', () => {
    beforeEach(() => {
      cy.visit(`${instrumentPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Instrument');
    });

    it('should create an instance of Instrument', () => {
      cy.get(`[data-cy="symbol"]`).type('around');
      cy.get(`[data-cy="symbol"]`).should('have.value', 'around');

      cy.get(`[data-cy="name"]`).type('pfft psst');
      cy.get(`[data-cy="name"]`).should('have.value', 'pfft psst');

      cy.get(`[data-cy="assetClass"]`).select('EQUITY');

      cy.get(`[data-cy="exchangeCode"]`).type('whenever turbulent');
      cy.get(`[data-cy="exchangeCode"]`).should('have.value', 'whenever turbulent');

      cy.get(`[data-cy="tickSize"]`).type('1891.47');
      cy.get(`[data-cy="tickSize"]`).should('have.value', '1891.47');

      cy.get(`[data-cy="lotSize"]`).type('5781');
      cy.get(`[data-cy="lotSize"]`).should('have.value', '5781');

      cy.get(`[data-cy="currency"]`).select('USD');

      cy.get(`[data-cy="status"]`).type('potentially meanwhile');
      cy.get(`[data-cy="status"]`).should('have.value', 'potentially meanwhile');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        instrument = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', instrumentPageUrlPattern);
    });
  });
});
