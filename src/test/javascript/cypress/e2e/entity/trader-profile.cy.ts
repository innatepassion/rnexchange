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

describe('TraderProfile e2e test', () => {
  const traderProfilePageUrl = '/trader-profile';
  const traderProfilePageUrlPattern = new RegExp('/trader-profile(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const traderProfileSample = { displayName: 'sizzle', email: 'Avery76@hotmail.com', kycStatus: 'APPROVED', status: 'INACTIVE' };

  let traderProfile;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/trader-profiles+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/trader-profiles').as('postEntityRequest');
    cy.intercept('DELETE', '/api/trader-profiles/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (traderProfile) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/trader-profiles/${traderProfile.id}`,
      }).then(() => {
        traderProfile = undefined;
      });
    }
  });

  it('TraderProfiles menu should load TraderProfiles page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('trader-profile');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('TraderProfile').should('exist');
    cy.url().should('match', traderProfilePageUrlPattern);
  });

  describe('TraderProfile page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(traderProfilePageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create TraderProfile page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/trader-profile/new$'));
        cy.getEntityCreateUpdateHeading('TraderProfile');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', traderProfilePageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/trader-profiles',
          body: traderProfileSample,
        }).then(({ body }) => {
          traderProfile = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/trader-profiles+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/trader-profiles?page=0&size=20>; rel="last",<http://localhost/api/trader-profiles?page=0&size=20>; rel="first"',
              },
              body: [traderProfile],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(traderProfilePageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details TraderProfile page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('traderProfile');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', traderProfilePageUrlPattern);
      });

      it('edit button click should load edit TraderProfile page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('TraderProfile');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', traderProfilePageUrlPattern);
      });

      it('edit button click should load edit TraderProfile page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('TraderProfile');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', traderProfilePageUrlPattern);
      });

      it('last delete button click should delete instance of TraderProfile', () => {
        cy.intercept('GET', '/api/trader-profiles/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('traderProfile').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', traderProfilePageUrlPattern);

        traderProfile = undefined;
      });
    });
  });

  describe('new TraderProfile page', () => {
    beforeEach(() => {
      cy.visit(`${traderProfilePageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('TraderProfile');
    });

    it('should create an instance of TraderProfile', () => {
      cy.get(`[data-cy="displayName"]`).type('masquerade mysteriously');
      cy.get(`[data-cy="displayName"]`).should('have.value', 'masquerade mysteriously');

      cy.get(`[data-cy="email"]`).type('Octavia.Boyer@hotmail.com');
      cy.get(`[data-cy="email"]`).should('have.value', 'Octavia.Boyer@hotmail.com');

      cy.get(`[data-cy="mobile"]`).type('how rigid inasmuch');
      cy.get(`[data-cy="mobile"]`).should('have.value', 'how rigid inasmuch');

      cy.get(`[data-cy="kycStatus"]`).select('PENDING');

      cy.get(`[data-cy="status"]`).select('ACTIVE');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        traderProfile = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', traderProfilePageUrlPattern);
    });
  });
});
