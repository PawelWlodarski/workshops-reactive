package jug.workshops.reactive.testing

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{TestActorRef, TestKit}
import akka.util.Timeout
import common.StopSystemAfterAll
import jug.workshops.reactive.testing.BrowserForTesting._
import org.scalatest.{MustMatchers, WordSpecLike}

import scala.util.Success

/**
  * Created by pawel on 15.10.16.
  */
class SynchronousTestingExercise extends TestKit(ActorSystem("TestingBrowser"))
  with WordSpecLike with MustMatchers with StopSystemAfterAll{

  "Browser" should {
    "store history url" in {
      //given
      val browser=TestActorRef(new BrowserForTesting(testActor))
      val url=Url("testPage")

      //when
      ???  // exercise

      //then
      browser.underlyingActor.browserHistory must contain(url)
    }

    "not store anonymous call" in {
      //given
      val browser:TestActorRef[BrowserForTesting] = ???
      val url=Url("testPage")

      //when
      browser ! AnonymousCall(url)

      //then
      ??? //mustBe empty
    }

    //should we test this in synchronous thread?
    "return browsing history" in {
      //given
      val browser:TestActorRef[BrowserForTesting] = ???
      val url1=Url("testPage1")
      val url2=Url("testPage2")
      val url3=Url("testPage3")

      browser ! Call(url1)
      browser ! Call(url2)
      browser ! Call(url3)
      //when
      import akka.pattern.ask

      import scala.concurrent.duration._
      implicit val timeout = Timeout(1 second)
      browser ? RequestForHistory

      //then
      val history:Seq[Url] = ???

      history must  contain allOf(url1,url2,url3)
    }

  }
}