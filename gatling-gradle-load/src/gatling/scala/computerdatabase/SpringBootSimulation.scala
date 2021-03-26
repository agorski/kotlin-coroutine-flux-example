package computerdatabase

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration._

class SpringBootSimulation extends Simulation {

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl("http://localhost:8080") // Here is the root for all relative URLs
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  val scnCoroutineMix: ScenarioBuilder = scenario("Scenario coroutine web and save")
    .exec(http("coroutine slow")
      .get("/coroutine/slow-json"))
    .exec(http("coroutine long and store")
      .get("/coroutine/long-json/store"))
    .exec(http("coroutine long")
      .get("/coroutine/long-json"))

  val scnCoroutineWebCall: ScenarioBuilder = scenario("Scenario coroutine web only")
    .exec(http("coroutine slow")
      .get("/coroutine/slow-json"))
    .exec(http("coroutine long")
      .get("/coroutine/long-json"))

  val scnFluxMix: ScenarioBuilder = scenario("Scenario flux web and save")
    .exec(http("flux slow")
      .get("/flux/slow-json"))
    .exec(http("flux long and store")
      .get("/flux/long-json/store"))
    .exec(http("flux long")
      .get("/flux/long-json"))

  val scnFluxWebCall: ScenarioBuilder = scenario("Scenario flux web only")
    .exec(http("flux slow")
      .get("/flux/slow-json"))
    .exec(http("flux long")
      .get("/flux/long-json"))

  setUp(
    /*
    scnFlux.inject(
      constantUsersPerSec(4).during(3.seconds),
      rampUsers(12).during(5.seconds),
      constantUsersPerSec(40).during(10.seconds).randomized,
    ).protocols(httpProtocol),
    scnCoroutineMix.inject(
      constantUsersPerSec(4).during(3.seconds),
      rampUsers(12).during(5.seconds),
      constantUsersPerSec(40).during(10.seconds).randomized,
    ).protocols(httpProtocol),

    scnCoroutineWebCall.inject(
      constantUsersPerSec(12).during(3.seconds),
      rampUsers(60).during(5.seconds),
      constantUsersPerSec(200).during(10.seconds).randomized,
    ).protocols(httpProtocol)
*/
    scnFluxWebCall.inject(
      constantUsersPerSec(12).during(3.seconds),
      rampUsers(60).during(5.seconds),
      constantUsersPerSec(200).during(10.seconds).randomized,
    ).protocols(httpProtocol)
  )
}
