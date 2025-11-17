/* eslint-disable @typescript-eslint/no-namespace */

// ***********************************************
// This commands.ts shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************

// ***********************************************
// Begin Specific Selector Attributes for Cypress
// ***********************************************

// Navbar
export const navbarSelector = '[data-cy="navbar"]';
export const adminMenuSelector = '[data-cy="adminMenu"]';
export const accountMenuSelector = '[data-cy="accountMenu"]';
export const registerItemSelector = '[data-cy="register"]';
export const settingsItemSelector = '[data-cy="settings"]';
export const passwordItemSelector = '[data-cy="passwordItem"]';
export const loginItemSelector = '[data-cy="login"]';
export const logoutItemSelector = '[data-cy="logout"]';
export const entityItemSelector = '[data-cy="entity"]';

// Login
export const titleLoginSelector = '[data-cy="loginTitle"]';
export const errorLoginSelector = '[data-cy="loginError"]';
export const usernameLoginSelector = '[data-cy="username"]';
export const passwordLoginSelector = '[data-cy="password"]';
export const forgetYourPasswordSelector = '[data-cy="forgetYourPasswordSelector"]';
export const submitLoginSelector = '[data-cy="submit"]';

// Register
export const usernameRegisterSelector = '[data-cy="username"]';
export const emailRegisterSelector = '[data-cy="email"]';
export const firstPasswordRegisterSelector = '[data-cy="firstPassword"]';
export const secondPasswordRegisterSelector = '[data-cy="secondPassword"]';
export const submitRegisterSelector = '[data-cy="submit"]';

// Settings
export const firstNameSettingsSelector = '[data-cy="firstname"]';
export const lastNameSettingsSelector = '[data-cy="lastname"]';
export const emailSettingsSelector = '[data-cy="email"]';
export const submitSettingsSelector = '[data-cy="submit"]';

// Password
export const currentPasswordSelector = '[data-cy="currentPassword"]';
export const newPasswordSelector = '[data-cy="newPassword"]';
export const confirmPasswordSelector = '[data-cy="confirmPassword"]';
export const submitPasswordSelector = '[data-cy="submit"]';

// Reset Password
export const emailResetPasswordSelector = '[data-cy="emailResetPassword"]';
export const submitInitResetPasswordSelector = '[data-cy="submit"]';

// Administration
export const userManagementPageHeadingSelector = '[data-cy="userManagementPageHeading"]';
export const swaggerFrameSelector = 'iframe[data-cy="swagger-frame"]';
export const swaggerPageSelector = '[id="swagger-ui"]';
export const metricsPageHeadingSelector = '[data-cy="metricsPageHeading"]';
export const healthPageHeadingSelector = '[data-cy="healthPageHeading"]';
export const logsPageHeadingSelector = '[data-cy="logsPageHeading"]';
export const configurationPageHeadingSelector = '[data-cy="configurationPageHeading"]';

// ***********************************************
// End Specific Selector Attributes for Cypress
// ***********************************************

export const classInvalid = 'is-invalid';

export const classValid = 'is-valid';

Cypress.Commands.add('authenticatedRequest', data => {
  const jwtToken = sessionStorage.getItem(Cypress.env('jwtStorageName'));
  const bearerToken = jwtToken && JSON.parse(jwtToken);
  if (bearerToken) {
    return cy.request({
      ...data,
      auth: {
        bearer: bearerToken,
      },
    });
  }
  return cy.request(data);
});

Cypress.Commands.add('login', (username: string, password: string) => {
  cy.session(
    [username, password],
    () => {
      cy.request({
        method: 'GET',
        url: '/api/account',
        failOnStatusCode: false,
      });
      cy.authenticatedRequest({
        method: 'POST',
        body: { username, password },
        url: Cypress.env('authenticationUrl'),
      }).then(({ body: { id_token } }) => {
        sessionStorage.setItem(Cypress.env('jwtStorageName'), JSON.stringify(id_token));
      });
    },
    {
      validate() {
        cy.authenticatedRequest({ url: '/api/account' }).its('status').should('eq', 200);
      },
    },
  );
});

/**
 * M6 Phase 2: Login as a demo user with role-specific assertions.
 *
 * @param role - One of 'trader', 'broker', or 'exchange'
 * @param password - Password for the demo user (defaults to 'admin' for all demo users)
 */
Cypress.Commands.add('loginAsDemoUser', (role: 'trader' | 'broker' | 'exchange', password: string = 'admin') => {
  const username = role === 'trader' ? 'trader_demo' : role === 'broker' ? 'broker_demo' : 'exchange_demo';
  cy.login(username, password);

  // Verify role-specific access
  cy.authenticatedRequest({ url: '/api/account' }).then(response => {
    expect(response.status).to.eq(200);
    const authorities = response.body.authorities || [];

    if (role === 'trader') {
      expect(authorities).to.include('ROLE_TRADER');
    } else if (role === 'broker') {
      expect(authorities).to.include('ROLE_BROKER_ADMIN');
    } else if (role === 'exchange') {
      expect(authorities).to.include('ROLE_EXCHANGE_OPERATOR');
    }
  });
});

/**
 * M6 Phase 2: Reset demo accounts to known baseline state.
 *
 * This command calls the baseline seed API to reset demo users and their accounts
 * to a known state. Requires EXCHANGE_OPERATOR role.
 *
 * @param force - Whether to force reset even if data exists (default: true)
 */
Cypress.Commands.add('resetDemoAccounts', (force: boolean = true) => {
  // First login as exchange_demo to get the required role
  cy.login('exchange_demo', 'admin');

  cy.authenticatedRequest({
    method: 'POST',
    url: '/api/admin/baseline-seed/run',
    body: {
      force: force,
      contexts: ['baseline'],
    },
  }).then(response => {
    expect(response.status).to.be.oneOf([200, 202]);
    // Wait a bit for the seed job to complete
    cy.wait(2000);
  });
});

declare global {
  namespace Cypress {
    interface Chainable {
      authenticatedRequest(data): Cypress.Chainable;
      login(username: string, password: string): Cypress.Chainable;
      loginAsDemoUser(role: 'trader' | 'broker' | 'exchange', password?: string): Cypress.Chainable;
      resetDemoAccounts(force?: boolean): Cypress.Chainable;
    }
  }
}

import 'cypress-audit/commands';
// Convert this to a module instead of a script (allows import/export)
export {};
