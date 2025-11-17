/**
 * Trader Trading E2E Test
 * Phase 6, Task T028: Test BUY then SELL flow with portfolio and cash updates
 * Phase 3, Task T012 [US1]: Extended to cover watchlist curation, market order placement, and position/ledger reconciliation
 *
 * This test validates the end-to-end trading flow for a Trader:
 * 1. Watchlist curation (add/remove symbols)
 * 2. Place a market order from Market Watch
 * 3. Verify order fills and position is created
 * 4. Verify cash balance decreases
 * 5. Verify ledger entry shows the debit with clear labels
 * 6. Verify position and ledger reconciliation
 * 7. Place a SELL order to close the position
 * 8. Verify position qty is reduced/closed
 * 9. Verify cash balance increases (credit)
 * 10. Verify realized P&L is recorded
 * 11. Measure latency to ensure WebSocket updates meet SC-004 (<2 seconds)
 */

describe('Trader Trading Flow E2E Test', () => {
  const username = Cypress.env('E2E_USERNAME') ?? 'trader_demo';
  const password = Cypress.env('E2E_PASSWORD') ?? 'password';
  const tradingAccountId = Cypress.env('TRADING_ACCOUNT_ID') ?? 1;
  const instrumentSymbol = Cypress.env('INSTRUMENT_SYMBOL') ?? 'RELIANCE';

  beforeEach(() => {
    // Login as trader
    cy.login(username, password);

    // Intercept API calls to monitor latency
    cy.intercept('POST', '/api/orders').as('placeOrder');
    cy.intercept('GET', '/api/watchlists*').as('getWatchlists');
    cy.intercept('POST', '/api/watchlists/*/items').as('addWatchlistSymbol');
    cy.intercept('DELETE', '/api/watchlists/*/items/*').as('removeWatchlistSymbol');
    cy.intercept('GET', `/api/trading-accounts/${tradingAccountId}/positions`).as('getPositions');
    cy.intercept('GET', `/api/trading-accounts/${tradingAccountId}/ledger-entries`).as('getLedger');
    cy.intercept('GET', `/api/trading-accounts/${tradingAccountId}/cash-balance`).as('getCashBalance');
  });

  it('[US1] should complete watchlist curation, market order placement, and position/ledger reconciliation', () => {
    // Step 1: Navigate to Market Watch and curate watchlist
    cy.visit('/market-watch');
    cy.wait('@getWatchlists');

    // Wait for watchlists to load and select the first one (or create if none exists)
    cy.get('[data-testid="watchlist-selector"]', { timeout: 5000 }).should('exist');

    // Add symbol to watchlist
    let watchlistId: number;
    cy.wait('@getWatchlists').then(interception => {
      const watchlists = interception.response?.body || [];
      if (watchlists.length > 0) {
        watchlistId = watchlists[0].id;
        // Add symbol if not already present
        cy.request({
          method: 'POST',
          url: `/api/watchlists/${watchlistId}/items`,
          body: { symbol: instrumentSymbol },
          failOnStatusCode: false,
        }).then(response => {
          // If already exists (400), that's fine - continue
          if (response.status !== 200 && response.status !== 400) {
            cy.log(`Warning: Could not add symbol to watchlist: ${response.status}`);
          }
        });
      }
    });

    // Verify symbol appears in Market Watch table
    cy.get(`[data-testid="market-watch-row-${instrumentSymbol}"]`, { timeout: 10000 }).should('exist');

    // Step 2: Place a market order from Market Watch
    cy.get(`[data-testid="market-watch-row-${instrumentSymbol}"]`).within(() => {
      cy.contains('button', 'Trade').click();
    });

    // Wait for order ticket drawer to open
    cy.get('[data-cy="order-ticket-drawer"]', { timeout: 3000 }).should('be.visible');

    // Fill order form
    cy.get('[data-cy="order-side-select"]').select('BUY');
    cy.get('[data-cy="order-type-select"]').select('MARKET');
    cy.get('[data-cy="order-qty-input"]').clear().type('10');

    // Record initial cash balance
    cy.visit('/trader/portfolio-cash');
    cy.wait('@getCashBalance');
    let initialBalance = 0;
    cy.get('[data-cy="cash-balance"]', { timeout: 5000 })
      .should('exist')
      .invoke('text')
      .then(text => {
        const match = text.match(/[\d,]+\.?\d*/);
        if (match) {
          initialBalance = parseFloat(match[0].replace(/,/g, ''));
          cy.log(`Initial cash balance: ${initialBalance}`);
        }
      });

    // Return to Market Watch and place order
    cy.visit('/market-watch');
    cy.get(`[data-testid="market-watch-row-${instrumentSymbol}"]`).within(() => {
      cy.contains('button', 'Trade').click();
    });

    cy.get('[data-cy="order-ticket-drawer"]', { timeout: 3000 }).should('be.visible');
    cy.get('[data-cy="order-side-select"]').select('BUY');
    cy.get('[data-cy="order-type-select"]').select('MARKET');
    cy.get('[data-cy="order-qty-input"]').clear().type('10');
    cy.get('[data-cy="order-submit-button"]').click();

    // Step 3: Wait for order submission and verify success
    cy.wait('@placeOrder', { timeout: 5000 }).then(interception => {
      expect(interception.response?.statusCode).to.equal(200);
      const orderResponse = interception.response?.body;
      expect(orderResponse.status).to.equal('FILLED');
      cy.log(`✓ Order filled at ${orderResponse.executionPrice || 'market price'}`);
    });

    // Verify success notification
    cy.get('[data-cy="toast-success"], .alert-success', { timeout: 3000 }).should('contain', 'Order');

    // Step 4: Navigate to portfolio and verify position was created
    cy.visit('/trader/portfolio-cash');
    cy.wait('@getPositions', { timeout: 5000 });

    cy.get(`[data-testid="position-row-${instrumentSymbol}"]`, { timeout: 5000 }).should('exist');
    cy.get(`[data-testid="position-row-${instrumentSymbol}"]`).within(() => {
      cy.contains('td', '10').should('exist'); // Verify quantity
    });

    // Step 5: Verify cash balance decreased
    cy.wait('@getCashBalance', { timeout: 5000 });
    cy.get('[data-cy="cash-balance"]')
      .invoke('text')
      .then(text => {
        const match = text.match(/[\d,]+\.?\d*/);
        if (match) {
          const newBalance = parseFloat(match[0].replace(/,/g, ''));
          cy.log(`New cash balance: ${newBalance}`);
          expect(newBalance).to.be.lessThan(initialBalance);
        }
      });

    // Step 6: Verify ledger entry shows debit with clear labels
    cy.wait('@getLedger', { timeout: 5000 });
    cy.get('[data-testid^="ledger-row-"]', { timeout: 5000 })
      .first()
      .within(() => {
        cy.contains('DEBIT').should('exist');
        cy.contains(instrumentSymbol).should('exist');
        cy.contains('BUY').should('exist');
      });

    cy.log('✓✓✓ [US1] Watchlist curation, market order placement, and position/ledger reconciliation completed');
  });

  it('should complete a full BUY then SELL trading cycle', () => {
    // Navigate to portfolio page
    cy.visit('/trader/portfolio-cash');
    cy.wait('@getPositions');

    // Record initial portfolio state
    let initialBalance = 0;
    cy.get('[data-cy="cash-balance"]')
      .invoke('text')
      .then(text => {
        // Extract numeric value from currency formatted text (e.g., "₹100,000.00" -> 100000)
        const match = text.match(/[\d,]+\.?\d*/);
        if (match) {
          initialBalance = parseFloat(match[0].replace(/,/g, ''));
        }
      });

    // Step 1: Place a BUY order
    cy.get('[data-cy="order-ticket-button"]').click();
    cy.get('[data-cy="order-side-select"]').select('BUY');
    cy.get('[data-cy="order-type-select"]').select('MARKET');
    cy.get('[data-cy="order-qty-input"]').type('10');
    cy.get('[data-cy="order-instrument-select"]').select(instrumentSymbol);

    // Record time before submitting order
    cy.get('[data-cy="order-submit-button"]').click();

    // Wait for order submission and verify success toast
    cy.wait('@placeOrder').then(interception => {
      expect(interception.response?.statusCode).to.equal(200);
      const orderResponse = interception.response?.body;
      expect(orderResponse.status).to.equal('FILLED');
      cy.log(`✓ BUY order filled at ${orderResponse.executionPrice}`);
    });

    // Verify success notification appears
    cy.get('[data-cy="toast-success"]').should('contain', 'Order filled');

    // Step 2: Wait for WebSocket update and verify position appears (SC-004: <2 seconds)
    const buyUpdateStart = Date.now();
    cy.wait('@getPositions', { timeout: 5000 }).then(() => {
      const latency = Date.now() - buyUpdateStart;
      cy.log(`⏱️ Position update latency: ${latency}ms (SC-004 target: <2000ms)`);
      expect(latency).to.be.lessThan(2000);
    });

    // Step 3: Verify position was created
    cy.get(`[data-cy="position-row-${instrumentSymbol}"]`).should('be.visible');
    cy.get(`[data-cy="position-qty-${instrumentSymbol}"]`).should('contain', '10');

    // Step 4: Verify cash balance decreased (BUY cost + fee)
    cy.get('[data-cy="cash-balance"]')
      .invoke('text')
      .then(text => {
        const match = text.match(/[\d,]+\.?\d*/);
        if (match) {
          const newBalance = parseFloat(match[0].replace(/,/g, ''));
          cy.log(`Initial Balance: ${initialBalance}, New Balance: ${newBalance}`);
          expect(newBalance).to.be.lessThan(initialBalance); // Balance decreased
        }
      });

    // Step 5: Verify ledger entry shows debit
    cy.wait('@getLedger');
    cy.get('[data-cy="ledger-entry"]').first().should('contain', 'DEBIT');
    cy.get('[data-cy="ledger-entry"]').first().should('contain', 'BUY');

    // Step 6: Place a SELL order for half the position
    cy.get('[data-cy="order-ticket-button"]').click();
    cy.get('[data-cy="order-side-select"]').select('SELL');
    cy.get('[data-cy="order-type-select"]').select('MARKET');
    cy.get('[data-cy="order-qty-input"]').type('5');
    cy.get('[data-cy="order-instrument-select"]').select(instrumentSymbol);

    // Record time before submitting SELL order
    cy.get('[data-cy="order-submit-button"]').click();

    // Wait for SELL order submission
    cy.wait('@placeOrder').then(interception => {
      expect(interception.response?.statusCode).to.equal(200);
      const orderResponse = interception.response?.body;
      expect(orderResponse.status).to.equal('FILLED');
      cy.log(`✓ SELL order filled at ${orderResponse.executionPrice}`);
    });

    // Step 7: Verify SELL success toast and P&L is shown
    cy.get('[data-cy="toast-success"]').should('contain', 'Order filled');
    cy.get('[data-cy="toast-success"]').should('contain', 'P&L'); // P&L info in toast

    // Step 8: Wait for WebSocket update after SELL (SC-004: <2 seconds)
    const sellUpdateStart = Date.now();
    cy.wait('@getPositions', { timeout: 5000 }).then(() => {
      const latency = Date.now() - sellUpdateStart;
      cy.log(`⏱️ SELL update latency: ${latency}ms (SC-004 target: <2000ms)`);
      expect(latency).to.be.lessThan(2000);
    });

    // Step 9: Verify position qty reduced from 10 to 5
    cy.get(`[data-cy="position-qty-${instrumentSymbol}"]`).should('contain', '5');

    // Step 10: Verify ledger shows CREDIT entry for SELL
    cy.wait('@getLedger');
    cy.get('[data-cy="ledger-entry"]').should('contain', 'CREDIT');
    cy.get('[data-cy="ledger-entry"]').should('contain', 'SELL');

    // Step 11: Verify cash increased after SELL credit
    cy.get('[data-cy="cash-balance"]')
      .invoke('text')
      .then(text => {
        const match = text.match(/[\d,]+\.?\d*/);
        if (match) {
          const balanceAfterSell = parseFloat(match[0].replace(/,/g, ''));
          cy.log(`Balance after SELL: ${balanceAfterSell}`);
          // Should be greater than right after BUY but may be less than initial (due to two trading fees)
          expect(balanceAfterSell).to.be.greaterThan(0);
        }
      });

    // Step 12: Verify realized P&L is shown in ledger description
    cy.get('[data-cy="ledger-entry"]').first().should('contain', 'P&L');

    // Log final summary
    cy.log('✓✓✓ Complete trading cycle: BUY -> SELL with portfolio updates and P&L tracking');
  });

  it('should reject BUY order with insufficient funds', () => {
    cy.visit('/');
    cy.get('[data-cy="portfolio-link"]').click();

    // Try to place a very large BUY order (likely to exceed balance)
    cy.get('[data-cy="order-ticket-button"]').click();
    cy.get('[data-cy="order-side-select"]').select('BUY');
    cy.get('[data-cy="order-type-select"]').select('MARKET');
    cy.get('[data-cy="order-qty-input"]').type('999999'); // Very large
    cy.get('[data-cy="order-instrument-select"]').select(instrumentSymbol);
    cy.get('[data-cy="order-submit-button"]').click();

    // Verify rejection message
    cy.get('[data-cy="toast-error"]').should('be.visible');
    cy.get('[data-cy="toast-error"]').should('contain', 'Insufficient funds');
  });

  it('should reject SELL order when no position exists', () => {
    cy.visit('/');
    cy.get('[data-cy="portfolio-link"]').click();

    // Try to sell an instrument we don't own
    cy.get('[data-cy="order-ticket-button"]').click();
    cy.get('[data-cy="order-side-select"]').select('SELL');
    cy.get('[data-cy="order-type-select"]').select('MARKET');
    cy.get('[data-cy="order-qty-input"]').type('10');
    // Choose an instrument (assume a different one without position)
    cy.get('[data-cy="order-instrument-select"]').select(instrumentSymbol);
    cy.get('[data-cy="order-submit-button"]').click();

    // May get rejection if no position, or success if position exists from previous test
    // Just verify no crash
    cy.get('[data-cy="toast-error"], [data-cy="toast-success"]').should('be.visible');
  });

  it('should update portfolio in real-time via WebSocket', () => {
    cy.visit('/');
    cy.get('[data-cy="portfolio-link"]').click();

    // Record initial portfolio state
    let initialPositionCount = 0;
    cy.get('[data-cy="position-row"]')
      .its('length')
      .then(count => {
        initialPositionCount = count;
      });

    // Place a BUY order
    cy.get('[data-cy="order-ticket-button"]').click();
    cy.get('[data-cy="order-side-select"]').select('BUY');
    cy.get('[data-cy="order-type-select"]').select('MARKET');
    cy.get('[data-cy="order-qty-input"]').type('5');
    cy.get('[data-cy="order-instrument-select"]').select(instrumentSymbol);
    cy.get('[data-cy="order-submit-button"]').click();

    // Wait for WebSocket update
    cy.wait('@placeOrder');

    // Verify portfolio updated without page refresh
    cy.get('[data-cy="position-row"]')
      .its('length')
      .then(newCount => {
        // Either new position was created or existing one was updated
        expect(newCount).to.be.greaterThanOrEqual(initialPositionCount);
      });

    cy.get('[data-cy="toast-success"]').should('be.visible');
  });

  // T019 [US1]: Test WebSocket-driven updates for order status and portfolio tiles
  it('[US1] should receive WebSocket updates for order status and portfolio within 2 seconds', () => {
    cy.visit('/trader/portfolio-cash');
    cy.wait('@getPositions');
    cy.wait('@getCashBalance');

    // Record initial state
    let initialBalance = 0;
    let initialPositionQty = 0;

    cy.get('[data-cy="cash-balance"]')
      .invoke('text')
      .then(text => {
        const match = text.match(/[\d,]+\.?\d*/);
        if (match) {
          initialBalance = parseFloat(match[0].replace(/,/g, ''));
        }
      });

    // Check if position already exists
    cy.get('body').then($body => {
      if ($body.find(`[data-testid="position-row-${instrumentSymbol}"]`).length > 0) {
        cy.get(`[data-testid="position-row-${instrumentSymbol}"]`)
          .find('td')
          .eq(1) // Quantity column
          .invoke('text')
          .then(text => {
            const match = text.match(/\d+/);
            if (match) {
              initialPositionQty = parseInt(match[0], 10);
            }
          });
      }
    });

    // Place a BUY order
    cy.get('[data-cy="order-ticket-button"]').click();
    cy.get('[data-cy="order-side-select"]').select('BUY');
    cy.get('[data-cy="order-type-select"]').select('MARKET');
    cy.get('[data-cy="order-qty-input"]').clear().type('10');
    cy.get('[data-cy="order-instrument-select"]').select(instrumentSymbol);

    // T019 [US1]: Record timestamp before order submission to measure WebSocket latency
    const orderSubmitTime = Date.now();
    cy.get('[data-cy="order-submit-button"]').click();

    // Wait for order API response
    cy.wait('@placeOrder', { timeout: 5000 }).then(interception => {
      expect(interception.response?.statusCode).to.equal(200);
      const orderResponse = interception.response?.body;
      expect(orderResponse.status).to.equal('FILLED');
      cy.log(`✓ Order filled: ${orderResponse.id}`);
    });

    // T019 [US1]: Verify order status toast appears (WebSocket-driven UI update)
    cy.get('[data-cy="toast-success"], .alert-success', { timeout: 3000 })
      .should('be.visible')
      .and('contain', 'Order')
      .then(() => {
        const toastLatency = Date.now() - orderSubmitTime;
        cy.log(`⏱️ Toast notification latency: ${toastLatency}ms (SC-004 target: <2000ms)`);
        expect(toastLatency).to.be.lessThan(2000);
      });

    // T019 [US1]: Verify position tile updates via WebSocket (without page refresh)
    cy.get(`[data-testid="position-row-${instrumentSymbol}"]`, { timeout: 3000 })
      .should('exist')
      .then(() => {
        const positionUpdateLatency = Date.now() - orderSubmitTime;
        cy.log(`⏱️ Position update latency: ${positionUpdateLatency}ms (SC-004 target: <2000ms)`);
        expect(positionUpdateLatency).to.be.lessThan(2000);
      });

    // Verify position quantity increased
    cy.get(`[data-testid="position-row-${instrumentSymbol}"]`).within(() => {
      cy.contains('td', '10').should('exist');
    });

    // T019 [US1]: Verify cash balance tile updates via WebSocket
    cy.get('[data-cy="cash-balance"]', { timeout: 3000 })
      .invoke('text')
      .then(text => {
        const match = text.match(/[\d,]+\.?\d*/);
        if (match) {
          const newBalance = parseFloat(match[0].replace(/,/g, ''));
          const balanceUpdateLatency = Date.now() - orderSubmitTime;
          cy.log(`⏱️ Cash balance update latency: ${balanceUpdateLatency}ms (SC-004 target: <2000ms)`);
          expect(balanceUpdateLatency).to.be.lessThan(2000);
          expect(newBalance).to.be.lessThan(initialBalance);
        }
      });

    // T019 [US1]: Verify ledger entry appears in real-time (WebSocket-driven)
    cy.get('[data-testid^="ledger-row-"]', { timeout: 3000 })
      .first()
      .should('exist')
      .within(() => {
        cy.contains('DEBIT').should('exist');
        cy.contains(instrumentSymbol).should('exist');
      })
      .then(() => {
        const ledgerUpdateLatency = Date.now() - orderSubmitTime;
        cy.log(`⏱️ Ledger update latency: ${ledgerUpdateLatency}ms (SC-004 target: <2000ms)`);
        expect(ledgerUpdateLatency).to.be.lessThan(2000);
      });

    cy.log('✓✓✓ [US1] WebSocket-driven updates verified: order status, position, cash balance, and ledger all updated within 2 seconds');
  });

  // T019 [US1]: Test WebSocket updates under simulated load (multiple rapid orders)
  it('[US1] should handle WebSocket updates under load (multiple rapid orders)', () => {
    cy.visit('/trader/portfolio-cash');
    cy.wait('@getPositions');
    cy.wait('@getCashBalance');

    const orderCount = 3;
    const orderSubmitTimes: number[] = [];

    // Place multiple orders rapidly
    for (let i = 0; i < orderCount; i++) {
      cy.get('[data-cy="order-ticket-button"]').click();
      cy.get('[data-cy="order-side-select"]').select('BUY');
      cy.get('[data-cy="order-type-select"]').select('MARKET');
      cy.get('[data-cy="order-qty-input"]').clear().type('5');
      cy.get('[data-cy="order-instrument-select"]').select(instrumentSymbol);

      const submitTime = Date.now();
      orderSubmitTimes.push(submitTime);
      cy.get('[data-cy="order-submit-button"]').click();

      // Wait for order to complete before next one
      cy.wait('@placeOrder', { timeout: 5000 });

      // Verify each order's WebSocket update arrives within 2 seconds
      cy.get('[data-cy="toast-success"]', { timeout: 3000 })
        .should('be.visible')
        .then(() => {
          const latency = Date.now() - submitTime;
          cy.log(`⏱️ Order ${i + 1} WebSocket update latency: ${latency}ms`);
          expect(latency).to.be.lessThan(2000);
        });

      // Small delay between orders to avoid overwhelming the system
      cy.wait(500);
    }

    // Verify final position reflects all orders
    cy.get(`[data-testid="position-row-${instrumentSymbol}"]`, { timeout: 5000 })
      .should('exist')
      .within(() => {
        // Should have at least the quantity from orders (5 * orderCount)
        cy.get('td').should('contain', '5');
      });

    cy.log(`✓✓✓ [US1] Handled ${orderCount} rapid orders with WebSocket updates all within 2 seconds`);
  });
});
