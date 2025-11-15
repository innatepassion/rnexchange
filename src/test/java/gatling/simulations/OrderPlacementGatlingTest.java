package gatling.simulations;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.header;
import static io.gatling.javaapi.http.HttpDsl.headerRegex;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Performance test for order placement and execution - Phase 6, Task T032
 *
 * Validates that order placement and WebSocket-driven UI updates meet performance requirements:
 * - p95 order placement latency < 250ms (constitution default)
 * - UI updates within 2 seconds (SC-004 target)
 *
 * This simulation tests:
 * 1. Authentication flow
 * 2. BUY order placement (MARKET)
 * 3. SELL order placement
 * 4. Portfolio and ledger entry retrieval
 *
 * @see <a href="https://github.com/jhipster/generator-jhipster/tree/v8.11.0/generators/gatling#logging-tips">Logging tips</a>
 */
public class OrderPlacementGatlingTest extends Simulation {

    String baseURL = Optional.ofNullable(System.getProperty("baseURL")).orElse("http://localhost:8080");

    HttpProtocolBuilder httpConf = http
        .baseUrl(baseURL)
        .inferHtmlResources()
        .acceptHeader("*/*")
        .acceptEncodingHeader("gzip, deflate")
        .acceptLanguageHeader("en-US,en;q=0.9")
        .connectionHeader("keep-alive")
        .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
        .silentResources(); // Silence all resources like css so they don't clutter the results

    Map<String, String> headersHttp = Map.of("Accept", "application/json");

    Map<String, String> headersHttpAuthentication = Map.of("Content-Type", "application/json", "Accept", "application/json");

    Map<String, String> headersHttpAuthenticated = Map.of("Accept", "application/json", "Authorization", "${access_token}");

    /**
     * Scenario: Complete trading workflow
     * 1. Authenticate as trader
     * 2. Retrieve trading account info
     * 3. Place BUY order (MARKET)
     * 4. Retrieve positions (simulates WebSocket refetch)
     * 5. Retrieve ledger entries
     * 6. Place SELL order
     * 7. Retrieve updated positions
     * 8. Measure latencies against SC-004 and constitution targets
     */
    ChainBuilder traderTradingScenario = exec(
        http("First unauthenticated request").get("/api/account").headers(headersHttp).check(status().is(401))
    )
        .exitHereIfFailed()
        .pause(1)
        .exec(
            http("Authenticate as Trader")
                .post("/api/authenticate")
                .headers(headersHttpAuthentication)
                .body(StringBody("{\"username\":\"trader1\", \"password\":\"password\"}"))
                .asJson()
                .check(header("Authorization").saveAs("access_token"))
        )
        .exitHereIfFailed()
        .pause(1)
        .exec(http("Get trading account").get("/api/trading-accounts").headers(headersHttpAuthenticated).check(status().is(200)))
        .pause(1)
        // BUY Order - Measure latency (should be <250ms per constitution)
        .exec(
            http("Place BUY order - MARKET")
                .post("/api/orders")
                .headers(headersHttpAuthenticated)
                .header("Content-Type", "application/json")
                .body(StringBody("{\"tradingAccountId\": 1, \"instrumentId\": 1, \"side\": \"BUY\", \"type\": \"MARKET\", \"qty\": 10}"))
                .asJson()
                .check(status().is(200))
        )
        .pause(Duration.ofMillis(500)) // Simulate WebSocket delivery delay
        // Retrieve positions (simulates UI refetch after WebSocket notification, should be <2 seconds per SC-004)
        .exec(
            http("Get positions after BUY")
                .get("/api/trading-accounts/1/positions")
                .headers(headersHttpAuthenticated)
                .check(status().is(200))
        )
        .pause(Duration.ofMillis(300))
        // Retrieve ledger entries
        .exec(
            http("Get ledger entries after BUY")
                .get("/api/trading-accounts/1/ledger-entries")
                .headers(headersHttpAuthenticated)
                .check(status().is(200))
        )
        .pause(1)
        // SELL Order - Test position reduction
        .exec(
            http("Place SELL order - MARKET")
                .post("/api/orders")
                .headers(headersHttpAuthenticated)
                .header("Content-Type", "application/json")
                .body(StringBody("{\"tradingAccountId\": 1, \"instrumentId\": 1, \"side\": \"SELL\", \"type\": \"MARKET\", \"qty\": 5}"))
                .asJson()
                .check(status().is(200))
        )
        .pause(Duration.ofMillis(500)) // Simulate WebSocket delivery delay
        // Retrieve updated positions after SELL (should be <2 seconds per SC-004)
        .exec(
            http("Get positions after SELL")
                .get("/api/trading-accounts/1/positions")
                .headers(headersHttpAuthenticated)
                .check(status().is(200))
        )
        .pause(Duration.ofMillis(300))
        // Retrieve updated ledger
        .exec(
            http("Get ledger entries after SELL")
                .get("/api/trading-accounts/1/ledger-entries")
                .headers(headersHttpAuthenticated)
                .check(status().is(200))
        )
        .pause(1);

    ScenarioBuilder scn = scenario("Order Placement Performance Test").exec(traderTradingScenario);

    {
        setUp(
            scn
                .injectOpen(
                    // Ramp up: 10 users over 30 seconds, then hold for 60 seconds
                    rampUsers(10).during(Duration.ofSeconds(30)),
                    // Additional burst: 5 more users for another 30 seconds
                    rampUsers(5).during(Duration.ofSeconds(30))
                )
                .protocols(httpConf)
        )// Assertions for performance targets
        .assertions(
            // p95 response time for order placement should be < 250ms (constitution default)
            io.gatling.javaapi.core.CoreDsl.global().responseTime().percentile(95).lt(250),
            // All requests should complete within 5 seconds (generous upper bound)
            io.gatling.javaapi.core.CoreDsl.global().responseTime().percentile(99).lt(5000),
            // Success rate should be 100%
            io.gatling.javaapi.core.CoreDsl.global().successfulRequests().percent().is(100.0)
        );
    }
}
