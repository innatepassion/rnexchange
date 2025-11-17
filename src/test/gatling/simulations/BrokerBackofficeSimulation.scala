import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import scala.util.Random

/**
 * M6 Phase 8 (T058): Extended Gatling performance simulation for broker backoffice flows.
 * Covers broker funds journal operations with M6 load targets.
 *
 * Performance targets:
 * - Broker journal operations should complete within reasonable latency
 * - Supports demo-scale load (hundreds of concurrent broker admins)
 */
class BrokerBackofficeSimulation extends Simulation {

  private val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  // Authentication scenario for broker admin
  private val authenticate = exec(
    http("Authenticate Broker Admin")
      .post("/api/authenticate")
      .body(StringBody("""{"username":"broker_demo","password":"broker_demo"}"""))
      .check(status.is(200))
      .check(jsonPath("$.id_token").saveAs("access_token"))
  )

  private val overview = scenario("Broker Overview")
    .exec(authenticate)
    .exec(
      http("GET /api/broker/overview")
        .get("/api/broker/overview")
        .header("Authorization", "Bearer ${access_token}")
        .check(status.is(200))
    )

  private val traders = scenario("Broker Traders")
    .exec(authenticate)
    .exec(
      http("GET /api/broker/traders")
        .get("/api/broker/traders?page=0&size=20")
        .header("Authorization", "Bearer ${access_token}")
        .check(status.is(200))
    )

  // M6: Broker funds journal flow
  private val fundsJournal = scenario("Broker Funds Journal")
    .exec(authenticate)
    .exec(
      http("POST /api/ledger-entries - Create Journal Entry")
        .post("/api/ledger-entries")
        .header("Authorization", "Bearer ${access_token}")
        .body(StringBody("""{
          "type": "CREDIT",
          "amount": 1000.00,
          "ccy": "USD",
          "description": "Performance test journal entry"
        }"""))
        .check(status.in(201, 200))
        .check(jsonPath("$.id").exists)
    )

  // M6: Broker journal endpoint (OpenAPI-generated)
  private val brokerJournal = scenario("Broker Journal API")
    .exec(authenticate)
    .exec(
      http("POST /api/broker/traders/{id}/journal")
        .post("/api/broker/traders/00000000-0000-0000-0000-000000000000/journal")
        .header("Authorization", "Bearer ${access_token}")
        .header("Idempotency-Key", s"perf-test-${Random.alphanumeric.take(16).mkString}")
        .body(StringBody("""{
          "direction": "credit",
          "amount": 500.00,
          "reason": "Performance test"
        }"""))
        .check(status.in(200, 201))
        .check(jsonPath("$.ledgerEntry.id").exists)
    )

  // M6 Performance targets: Support hundreds of concurrent broker admins
  // p95 latency for journal operations should be < 500ms
  setUp(
    overview.inject(constantUsersPerSec(5) during (60.seconds)),
    traders.inject(constantUsersPerSec(5) during (60.seconds)),
    fundsJournal.inject(
      rampUsers(100) during (30.seconds),
      constantUsersPerSec(10) during (60.seconds)
    ),
    brokerJournal.inject(
      rampUsers(50) during (30.seconds),
      constantUsersPerSec(5) during (60.seconds)
    )
  ).protocols(httpProtocol)
    .assertions(
      // M6: Broker journal operations should complete quickly
      details("POST /api/ledger-entries - Create Journal Entry").responseTime.percentile3.lt(500), // p95 < 500ms
      details("POST /api/broker/traders/{id}/journal").responseTime.percentile3.lt(500), // p95 < 500ms
      // Error rate should be low
      global.failedRequests.percent.lt(2.0), // < 2% error rate
      global.successfulRequests.percent.gt(98.0) // > 98% success rate
    )
}


