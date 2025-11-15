package gatling.simulations;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
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
 * Approximate WebSocket reconnect SLA simulation for NFR-002 (T124d/T124e).
 *
 * <p>Gatling's Java DSL does not drive the full STOMP/WebSocket handshake used by the UI in this
 * project, but we can still codify an HTTP-level proxy for reconnect behaviour:
 *
 * <ul>
 *   <li>Authenticate once as a trader-like principal.
 *   <li>Execute many short "disconnect/reconnect" cycles by polling the mock feed status endpoint
 *       after pauses.
 *   <li>Assert that the 99th percentile response time stays below 30 seconds, mirroring the
 *       reconnect SLA for real clients.
 * </ul>
 *
 * <p>To evolve this into a true WebSocket reconnect test, replace the status polling block with a
 * WS/STOMP scenario that repeatedly connects, subscribes, and disconnects while keeping the same
 * latency assertions.
 */
public class MockMarketDataReconnectGatlingTest extends Simulation {

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
        // Authenticate once as admin (proxy for a high-privilege user controlling/observing the feed)
        exec(http("Unauthenticated account request (reconnect)").get("/api/account").headers(headersHttp).check(status().is(401)))
            .exitHereIfFailed()
            .pause(2)
            .exec(
                http("Authenticate admin for reconnect test")
                    .post("/api/authenticate")
                    .headers(headersHttpAuthentication)
                    .body(StringBody("{\"username\":\"admin\", \"password\":\"admin\"}"))
                    .asJson()
                    .check(io.gatling.javaapi.http.HttpDsl.header("Authorization").saveAs("access_token"))
            )
            .exitHereIfFailed()
            .pause(2)
            .exec(
                http("Authenticated account request (reconnect)")
                    .get("/api/account")
                    .headers(headersHttpAuthenticated)
                    .check(status().is(200))
            )
            // Simulate many "reconnect" cycles by polling status after variable pauses.
            // In a real WS scenario, these pauses would map to disconnect windows.
            .repeat(1000)
            .on(
                exec(
                    http("Reconnect-style status poll")
                        .get("/api/marketdata/mock/status")
                        .headers(headersHttpAuthenticated)
                        .check(status().is(200))
                )// Randomised backoff between polls to emulate reconnect jitter
                .pause(Duration.ofSeconds(1), Duration.ofSeconds(5))
            );

    ScenarioBuilder users = scenario("Mock feed reconnect SLA (HTTP proxy)").exec(scn);

    {
        setUp(users.injectOpen(rampUsers(Integer.getInteger("users", 500)).during(Duration.ofSeconds(Integer.getInteger("ramp", 30)))))
            .protocols(httpConf)
            .assertions(
                // NFR-002 proxy: 99th percentile "reconnect" latency must remain under 30 seconds.
                io.gatling.javaapi.core.CoreDsl.global().responseTime().percentile(99).lt(30_000),
                io.gatling.javaapi.core.CoreDsl.global().successfulRequests().percent().gt(99.0)
            );
    }
}
