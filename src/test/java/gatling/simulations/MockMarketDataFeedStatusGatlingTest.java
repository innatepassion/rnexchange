package gatling.simulations;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
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
 * Phase 3B throughput and operator-control SLA Gatling simulation.
 *
 * <p>This scenario exercises:
 *
 * <ul>
 *   <li>T124: Load test around the mock feed status/controls with latency &amp; success-rate
 *       assertions.
 *   <li>T124a: Telemetry checks on the feed status contract (presence of ticks-per-second
 *       metrics).
 *   <li>T124b/T124c: Operator control endpoints {@code POST /api/marketdata/mock/start} and
 *       {@code /stop} under concurrent load with p95 &lt;= 2s (we use a stricter 500ms bound for
 *       status calls here).
 * </ul>
 *
 * <p>Note: The exact tick-per-second throughput (10,000 updates/sec) is driven by the running
 * environment and feed configuration. This simulation codifies latency and success-rate gates and
 * validates that status telemetry is present so CI can be wired to fail on regressions.
 */
public class MockMarketDataFeedStatusGatlingTest extends Simulation {

    String baseURL = Optional.ofNullable(System.getProperty("baseURL")).orElse("http://localhost:8080");

    HttpProtocolBuilder httpConf = http
        .baseUrl(baseURL)
        .inferHtmlResources()
        .acceptHeader("*/*")
        .acceptEncodingHeader("gzip, deflate")
        .acceptLanguageHeader("en-US,en;q=0.5")
        .connectionHeader("keep-alive")
        .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:123.0) Gecko/20100101 Firefox/123.0")
        .silentResources();

    Map<String, String> headersHttp = Map.of("Accept", "application/json");

    Map<String, String> headersHttpAuthentication = Map.of("Content-Type", "application/json", "Accept", "application/json");

    Map<String, String> headersHttpAuthenticated = Map.of("Accept", "application/json", "Authorization", "${access_token}");

    ChainBuilder scn =
        // Baseline unauthenticated/account checks + admin authentication (matches other Gatling tests)
        exec(http("Unauthenticated account request").get("/api/account").headers(headersHttp).check(status().is(401)))
            .exitHereIfFailed()
            .pause(5)
            .exec(
                http("Authenticate as admin")
                    .post("/api/authenticate")
                    .headers(headersHttpAuthentication)
                    .body(StringBody("{\"username\":\"admin\", \"password\":\"admin\"}"))
                    .asJson()
                    .check(header("Authorization").saveAs("access_token"))
            )
            .exitHereIfFailed()
            .pause(2)
            .exec(http("Authenticated account request").get("/api/account").headers(headersHttpAuthenticated).check(status().is(200)))
            .pause(2)
            // Start the mock feed (operator control path)
            .exec(
                http("Start mock feed")
                    .post("/api/marketdata/mock/start")
                    .headers(headersHttpAuthenticated)
                    .check(status().is(200))
                    // ensure globalState reflects RUNNING so we know the feed is active
                    .check(jsonPath("$.globalState").is("RUNNING"))
            )
            .pause(2)
            // Poll status repeatedly under load to exercise throughput and status contract
            .repeat(20)
            .on(
                exec(
                    http("Get mock feed status")
                        .get("/api/marketdata/mock/status")
                        .headers(headersHttpAuthenticated)
                        .check(status().is(200))
                        // Telemetry presence: at least one exchange status with ticksPerSecond field
                        .check(jsonPath("$.exchanges[0].exchangeCode").exists())
                        .check(jsonPath("$.exchanges[0].ticksPerSecond").exists())
                )// small think-time to mimic operator dashboard polling (approx every 2s)
                .pause(Duration.ofSeconds(2))
            )
            // Stop the mock feed and verify STOPPED state surfaces promptly
            .exec(
                http("Stop mock feed")
                    .post("/api/marketdata/mock/stop")
                    .headers(headersHttpAuthenticated)
                    .check(status().is(200))
                    .check(jsonPath("$.globalState").is("STOPPED"))
            );

    ScenarioBuilder users = scenario("Mock feed status and operator control SLA").exec(scn);

    {
        setUp(
            // Default: 1000 concurrent virtual users over 30 seconds, override with -Dusers / -Dramp
            users.injectOpen(rampUsers(Integer.getInteger("users", 1000)).during(Duration.ofSeconds(Integer.getInteger("ramp", 30))))
        )
            .protocols(httpConf)
            .assertions(
                // NFR-001 / NFR-005: 95th percentile latency for status/control endpoints well under 2s (use 500ms bound)
                io.gatling.javaapi.core.CoreDsl.global().responseTime().percentile(95).lt(500),
                // Message loss proxy: at least 99.9% of requests succeed (<0.1% failures)
                io.gatling.javaapi.core.CoreDsl.global().successfulRequests().percent().gt(99.9)
            );
    }
}
