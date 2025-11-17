import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

/**
 * M6 Phase 8 (T058): EOD Large Dataset Performance Test.
 *
 * Tests EOD settlement performance for ~10,000 positions to verify it completes within 5 minutes.
 * This simulation should be run separately from other load tests as it's a long-running operation.
 *
 * Performance target (NFR-001, SC-003):
 * - EOD settlement for ~10,000 positions completes within 5 minutes
 *
 * Usage:
 *   ./mvnw gatling:test -Dgatling.simulationClass=com.rnexchange.gatling.simulations.EodLargeDatasetSimulation
 */
class EodLargeDatasetSimulation extends Simulation {

  private val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  // Exchange Operator authentication
  private val authenticate = exec(
    http("Authenticate Exchange Operator")
      .post("/api/authenticate")
      .body(StringBody("""{"username":"exchange_demo","password":"exchange_demo"}"""))
      .check(status.is(200))
      .check(jsonPath("$.id_token").saveAs("access_token"))
  )

  // M6: EOD performance test for large dataset
  // This assumes the database has been seeded with ~10,000 positions
  private val runEodLarge = scenario("Run EOD - Large Dataset (10K Positions)")
    .exec(authenticate)
    .exec(
      http("POST /api/settlements/eod - Large Dataset")
        .post("/api/settlements/eod?date=2025-01-15")
        .header("Authorization", "Bearer ${access_token}")
        .check(status.in(202, 200))
        .check(jsonPath("$.id").exists)
        .check(jsonPath("$.refDate").is("2025-01-15"))
    )

  // Single EOD run - verify it completes within 5 minutes
  setUp(
    runEodLarge.inject(atOnceUsers(1))
  ).protocols(httpProtocol)
    .assertions(
      // M6: EOD for ~10,000 positions should complete within 5 minutes (300 seconds)
      global.responseTime.max.lt(300000), // Max response time < 5 minutes (300,000 ms)
      // Should complete successfully
      global.successfulRequests.count.is(1), // Exactly 1 successful request
      global.failedRequests.count.is(0) // No failures
    )
}

