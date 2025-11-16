import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BrokerBackofficeSimulation extends Simulation {

  private val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")

  private val overview = scenario("Broker Overview")
    .exec(
      http("GET /api/broker/overview")
        .get("/api/broker/overview?brokerId=1")
        .check(status.is(200))
    )

  private val traders = scenario("Broker Traders")
    .exec(
      http("GET /api/broker/traders")
        .get("/api/broker/traders?page=0&size=20&brokerId=1")
        .check(status.is(200))
    )

  setUp(
    overview.inject(constantUsersPerSec(2) during (30.seconds)),
    traders.inject(constantUsersPerSec(2) during (30.seconds))
  ).protocols(httpProtocol)
}


