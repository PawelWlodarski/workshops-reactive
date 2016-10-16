package jug.workshops.reactive.testing

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import common.StopSystemAfterAll
import jug.workshops.reactive.testing.BrowserForTesting.{AnonymousCall, Call, Response, Url}
import org.scalatest.{FeatureSpecLike, GivenWhenThen, MustMatchers}

class EndToEndBrowserTest extends TestKit(ActorSystem("integration-browser")) with FeatureSpecLike with GivenWhenThen
  with StopSystemAfterAll with MustMatchers with ImplicitSender{  //implicit sender!!

  info("For Existing page user should receive Response(200,body)")
  info("For Missing page user should receive Response(404,body)")
  info("For Wrong calls user should receive Response(500,body)")
  info("There is an error in internet mock")

  feature("All pages are returned to caller"){
    Given("Browser Is Connected To The Internet")
    val internet=system.actorOf(???,"internet")
    val browser = system.actorOf(???,"browser")


    When(
      """user calls  :
        |   --> correct
        |   --> wrong
        |   --> correct anonymous
        |   --> wrong
        |   --> missing
        |   --> correct
        |   --> wrong
      """.stripMargin)
    browser ! Call(Url("www.google.pl"))
    browser ! "Wrong"
    browser ! AnonymousCall(Url("www.trolling.com"))
    browser ! List(69,96)
    browser ! Call(Url("test3"))
    browser ! Call(Url("www.google.pl"))
    browser ! 42

    import scala.concurrent.duration._
    val clientMessages=receiveN(???,1 second)


    val expectedStatuses = Seq(200, 500, 200, 500, 404, 200, 500)
    Then(s"cliend will receive n any order $expectedStatuses")
    clientMessages.collect{case Response(code,body) => code}
      .groupBy(identity)
      .mapValues(_.size)
      .toSeq must contain allOf(???, ???, ???)   //use worksheet to experiment with this construction
  }
}
