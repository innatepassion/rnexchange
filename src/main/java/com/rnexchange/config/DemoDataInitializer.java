package com.rnexchange.config;

import com.rnexchange.service.seed.BaselineSeedService;
import com.rnexchange.service.seed.dto.BaselineSeedRequest;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * M6 Phase 2: Utility for initializing and resetting demo user accounts.
 *
 * <p>This component provides methods to reset demo accounts (trader_demo, broker_demo,
 * exchange_demo) to a known baseline state. It is used by:
 * <ul>
 *   <li>Cypress E2E tests: via {@code cy.resetDemoAccounts()} command</li>
 *   <li>Gatling performance tests: via direct API calls or programmatic invocation</li>
 *   <li>Manual demo preparation: via REST endpoint or direct service call</li>
 * </ul>
 * </p>
 *
 * <p>The reset operation uses the existing baseline seed infrastructure to ensure
 * demo users and their trading accounts are restored to deterministic starting
 * balances and positions.</p>
 *
 * @see BaselineSeedService
 * @see BaselineSeedCleanupRunner
 */
@Component
public class DemoDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final BaselineSeedService baselineSeedService;

    public DemoDataInitializer(BaselineSeedService baselineSeedService) {
        this.baselineSeedService = baselineSeedService;
    }

    /**
     * Reset demo accounts to baseline state.
     *
     * <p>This method triggers a baseline seed run with the 'baseline' context,
     * which will:
     * <ul>
     *   <li>Clean up existing demo user data (if force=true)</li>
     *   <li>Recreate demo users (trader_demo, broker_demo, exchange_demo)</li>
     *   <li>Restore trading accounts with starting balances (1,000,000 INR for trader_demo)</li>
     *   <li>Reset positions and ledger entries to baseline state</li>
     * </ul>
     * </p>
     *
     * @param force If true, force cleanup and reseed even if data exists
     * @return UUID of the seed job invocation
     */
    public UUID resetDemoAccounts(boolean force) {
        UUID invocationId = UUID.randomUUID();
        log.info("Resetting demo accounts to baseline state (invocationId: {}, force: {})", invocationId, force);

        BaselineSeedRequest request = BaselineSeedRequest.builder().invocationId(invocationId).force(force).addContext("baseline").build();

        baselineSeedService.runBaselineSeedBlocking(request);

        log.info("Demo accounts reset completed (invocationId: {})", invocationId);
        return invocationId;
    }

    /**
     * Reset demo accounts to baseline state (non-forcing).
     *
     * <p>Convenience method that calls {@link #resetDemoAccounts(boolean)} with force=false.</p>
     *
     * @return UUID of the seed job invocation
     */
    public UUID resetDemoAccounts() {
        return resetDemoAccounts(false);
    }
}
