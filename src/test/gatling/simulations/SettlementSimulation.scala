import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

/**
 * Lightweight Gatling smoke test for EOD and settlements APIs.
 * Verifies p95 latency and error rate thresholds for settlement endpoints.
 */
class SettlementSimulation extends Simulation {

  private val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  // Authentication scenario
  private val authenticate = exec(
    http("Authenticate")
      .post("/api/authenticate")
      .body(StringBody("""{"username":"admin","password":"admin"}"""))
      .check(status.is(200))
      .check(jsonPath("$.id_token").saveAs("access_token"))
  )

  // Exchange Operator scenarios
  private val listSettlements = scenario("List Settlement Batches")
    .exec(authenticate)
    .exec(
      http("GET /api/settlements")
        .get("/api/settlements?from=2025-01-01&to=2025-12-31")
        .header("Authorization", "Bearer ${access_token}")
        .check(status.is(200))
        .check(jsonPath("$[*].id").exists)
    )

  private val runEod = scenario("Run EOD Settlement")
    .exec(authenticate)
    .exec(
      http("POST /api/settlements/eod")
        .post("/api/settlements/eod?date=2025-01-15")
        .header("Authorization", "Bearer ${access_token}")
        .check(status.in(202, 200)) // Accept both 202 Accepted and 200 OK
        .check(jsonPath("$.id").exists)
    )

  // Trader scenarios
  private val listStatements = scenario("List Trader Statements")
    .exec(authenticate)
    .exec(
      http("GET /api/statements")
        .get("/api/statements")
        .header("Authorization", "Bearer ${access_token}")
        .check(status.in(200, 403)) // May be 403 if user is not a trader
    )

  // Broker Admin scenarios
  private val listBrokerSettlements = scenario("List Broker Settlements")
    .exec(authenticate)
    .exec(
      http("GET /api/broker/settlements")
        .get("/api/broker/settlements")
        .header("Authorization", "Bearer ${access_token}")
        .check(status.in(200, 403)) // May be 403 if user is not a broker admin
    )

  // Smoke test setup: low load to verify basic functionality
  // p95 latency should be < 2 seconds for read operations
  // p95 latency should be < 30 seconds for EOD operation
  // Error rate should be < 1%
  setUp(
    listSettlements.inject(constantUsersPerSec(1) during (10.seconds)),
    listStatements.inject(constantUsersPerSec(1) during (10.seconds)),
    listBrokerSettlements.inject(constantUsersPerSec(1) during (10.seconds))
    // Note: EOD is excluded from constant load as it's a long-running operation
    // Run EOD separately with: runEod.inject(atOnceUsers(1))
  ).protocols(httpProtocol)
    .assertions(
      // Assert p95 latency for read operations
      global.responseTime.percentile3.lt(2000), // p95 < 2 seconds
      // Assert error rate
      global.failedRequests.percent.lt(1.0), // < 1% error rate
      // Assert all requests complete
      global.successfulRequests.percent.gt(99.0) // > 99% success rate
    )
}

