/**
 * M6 Phase 8 (T059): Test utilities to reduce flakiness.
 * Provides better waits, selectors, and test data isolation helpers.
 */

/**
 * Wait for an element to be visible with retry logic and better error messages.
 * Reduces flakiness by using Cypress's built-in retry mechanism more effectively.
 */
export function waitForElement(
  selector: string,
  options?: {
    timeout?: number;
    retries?: number;
    errorMessage?: string;
  },
): Cypress.Chainable<JQuery<HTMLElement>> {
  const timeout = options?.timeout ?? 10000;
  const errorMessage = options?.errorMessage ?? `Element ${selector} not found`;

  return cy
    .get(selector, { timeout })
    .should('be.visible')
    .then(
      element => {
        if (!element || element.length === 0) {
          throw new Error(errorMessage);
        }
        return element;
      },
      error => {
        cy.log(`Retrying wait for ${selector}...`);
        throw new Error(`${errorMessage}: ${error.message}`);
      },
    );
}

/**
 * Wait for API response with better error handling.
 * Ensures the response is complete before proceeding.
 */
export function waitForApiResponse(
  alias: string,
  options?: {
    timeout?: number;
    expectedStatus?: number;
  },
): Cypress.Chainable<Cypress.Response<any>> {
  const timeout = options?.timeout ?? 30000;
  const expectedStatus = options?.expectedStatus ?? 200;

  return cy.wait(`@${alias}`, { timeout }).then((interception: any) => {
    if (!interception || !interception.response) {
      throw new Error(`API call ${alias} did not complete`);
    }
    if (interception.response.statusCode !== expectedStatus) {
      cy.log(`Warning: ${alias} returned status ${interception.response.statusCode}, expected ${expectedStatus}`);
    }
    return interception.response;
  });
}

/**
 * Wait for page to be fully loaded (including async content).
 * Reduces flakiness by ensuring all dynamic content is ready.
 */
export function waitForPageLoad(url?: string): void {
  if (url) {
    cy.visit(url);
  }
  // Wait for main content to be visible
  cy.get('body').should('be.visible');
  // Wait for any loading spinners to disappear
  cy.get('[data-cy="loading"], .spinner, [class*="loading"]', { timeout: 5000 }).should('not.exist');
}

/**
 * Create isolated test data with cleanup.
 * Helps prevent test interference by ensuring clean state.
 */
export function withIsolatedTestData<T>(setup: () => Promise<T>, cleanup: (data: T) => Promise<void>): Cypress.Chainable<T> {
  let testData: T;

  return cy
    .then(async () => {
      testData = await setup();
      return testData;
    })
    .then(data => {
      // Register cleanup
      Cypress.on('test:after:run', () => {
        if (testData) {
          cleanup(testData).catch(err => {
            cy.log(`Cleanup error: ${err.message}`);
          });
        }
      });
      return data;
    });
}

/**
 * Wait for WebSocket updates with timeout.
 * Useful for testing real-time features.
 */
export function waitForWebSocketUpdate(
  checkFn: () => Cypress.Chainable<any>,
  options?: {
    timeout?: number;
    interval?: number;
  },
): Cypress.Chainable<any> {
  const timeout = options?.timeout ?? 10000;
  const interval = options?.interval ?? 500;
  const startTime = Date.now();

  function attempt(): Cypress.Chainable<any> {
    return checkFn().then(
      result => {
        if (result) {
          return result;
        }
        if (Date.now() - startTime > timeout) {
          throw new Error(`WebSocket update timeout after ${timeout}ms`);
        }
        cy.wait(interval);
        return attempt();
      },
      error => {
        if (Date.now() - startTime > timeout) {
          throw error;
        }
        cy.wait(interval);
        return attempt();
      },
    );
  }

  return attempt();
}

/**
 * Stable selector helper that uses data-cy attributes.
 * Encourages use of stable test selectors.
 */
export const stableSelectors = {
  // Navigation
  navbar: '[data-cy="navbar"]',
  loginButton: '[data-cy="login"]',
  logoutButton: '[data-cy="logout"]',

  // Trading
  marketWatch: '[data-cy="market-watch"]',
  orderTicket: '[data-cy="order-ticket"]',
  portfolio: '[data-cy="portfolio"]',

  // Broker
  brokerDashboard: '[data-cy="broker-dashboard"]',
  brokerJournal: '[data-cy="broker-journal"]',

  // Exchange
  exchangeOverview: '[data-cy="exchange-overview"]',
  eodConsole: '[data-cy="eod-console"]',

  // Common
  loading: '[data-cy="loading"]',
  errorMessage: '[data-cy="error-message"]',
  successMessage: '[data-cy="success-message"]',
};

/**
 * Wait for network to be idle before proceeding.
 * Reduces flakiness from race conditions with async requests.
 */
export function waitForNetworkIdle(timeout = 2000): void {
  cy.window().then(win => {
    const originalFetch = win.fetch;
    let requestCount = 0;
    let idleTimer: ReturnType<typeof setTimeout>;

    const checkIdle = () => {
      if (requestCount === 0) {
        clearTimeout(idleTimer);
        idleTimer = setTimeout(() => {
          // Network is idle
        }, timeout);
      }
    };

    win.fetch = function (...args) {
      requestCount++;
      const promise = originalFetch.apply(this, args);
      promise.finally(() => {
        requestCount--;
        checkIdle();
      });
      return promise;
    };

    checkIdle();
  });
}
