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

  // M6 Phase 8 (T058): Extended performance simulation for EOD and statement flows.
  // Performance targets:
  // - p95 order placement latency < 250 ms (trader trade flow)
  // - EOD settlement for ~10,000 positions completes within 5 minutes
  // - WebSocket tick throughput ~10,000 updates/sec (tested separately or via integration)

  // Trader authentication for order placement
  private val authenticateTrader = exec(
    http("Authenticate Trader")
      .post("/api/authenticate")
      .body(StringBody("""{"username":"trader_demo","password":"trader_demo"}"""))
      .check(status.is(200))
      .check(jsonPath("$.id_token").saveAs("trader_token"))
  )

  // M6: Trader trade flow - order placement
  private val placeOrder = scenario("Trader Place Order")
    .exec(authenticateTrader)
    .exec(
      http("POST /api/orders/trading - Place Market Order")
        .post("/api/orders/trading")
        .header("Authorization", "Bearer ${trader_token}")
        .body(StringBody("""{
          "side": "BUY",
          "type": "MARKET",
          "qty": 10,
          "tif": "DAY",
          "instrument": {
            "id": 1,
            "symbol": "RELIANCE"
          }
        }"""))
        .check(status.in(201, 200))
        .check(jsonPath("$.id").exists)
        .check(jsonPath("$.status").exists)
    )

  // M6: EOD performance test - single EOD run with large dataset
  // Note: This should be run separately as it's a long-running operation
  private val runEodLarge = scenario("Run EOD - Large Dataset")
    .exec(authenticate)
    .exec(
      http("POST /api/settlements/eod - Large Dataset")
        .post("/api/settlements/eod?date=2025-01-15")
        .header("Authorization", "Bearer ${access_token}")
        .check(status.in(202, 200))
        .check(jsonPath("$.id").exists)
    )

  // M6 Performance setup:
  // - Trader order placement: ~1,000 concurrent traders, 5-10 orders/sec
  // - EOD: Single run for large dataset (10,000 positions)
  // - Statement reads: Moderate load
  setUp(
    // Trader order placement at demo-scale load
    placeOrder.inject(
      rampUsers(1000) during (60.seconds), // Ramp up to 1000 concurrent traders
      constantUsersPerSec(8) during (120.seconds) // Maintain 8 orders/sec (5-10 range)
    ),
    // Read operations at moderate load
    listSettlements.inject(constantUsersPerSec(5) during (60.seconds)),
    listStatements.inject(constantUsersPerSec(10) during (60.seconds)),
    listBrokerSettlements.inject(constantUsersPerSec(5) during (60.seconds))
    // Note: EOD large dataset test should be run separately:
    // runEodLarge.inject(atOnceUsers(1))
  ).protocols(httpProtocol)
    .assertions(
      // M6: p95 order placement latency < 250 ms (NFR-001, SC-003)
      details("POST /api/orders/trading - Place Market Order").responseTime.percentile3.lt(250), // p95 < 250ms
      // Read operations should be fast
      global.responseTime.percentile3.lt(1000), // p95 < 1 second for reads
      // Error rate should be very low
      global.failedRequests.percent.lt(1.0), // < 1% error rate
      global.successfulRequests.percent.gt(99.0) // > 99% success rate
    )

  // M6: Separate setup for EOD large dataset test
  // This should be run with: -Dgatling.simulationClass=com.rnexchange.gatling.simulations.SettlementSimulationEodLarge
  // Or create a separate simulation file for EOD-only testing
}

