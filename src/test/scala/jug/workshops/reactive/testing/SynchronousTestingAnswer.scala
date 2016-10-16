package jug.workshops.reactive.testing

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import akka.util.Timeout
import common.StopSystemAfterAll
import jug.workshops.reactive.testing.BrowserForTesting._
import org.scalatest.{FunSpecLike, FunSuiteLike, MustMatchers, WordSpecLike}

import scala.util.Success

/**
  * Created by pawel on 15.10.16.
  */
class SynchronousTestingAnswer extends TestKit(ActorSystem("TestingBrowser"))
  with WordSpecLike with MustMatchers with StopSystemAfterAll{

  "Browser" should {
    "store history url" in {
      //given
      val browser=TestActorRef(new BrowserForTesting(testActor))
      val url=Url("testPage")

      //when
      browser ! Call(url)

      //then
      browser.underlyingActor.browserHistory must contain(url)
    }

    "not store anonymous call" in {
      //given
      val browser=TestActorRef(new BrowserForTesting(testActor))
      val url=Url("testPage")

      //when
      browser ! AnonymousCall(url)

      //then
      browser.underlyingActor.browserHistory mustBe empty
    }

    //should we test this in synchronous thread?
    "return browsing history" in {
      //given
      val browser=TestActorRef(new BrowserForTesting(testActor))
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
      val request=browser ? RequestForHistory

      //then
      val Success(history)=request.mapTo[List[Url]].value.get

      history must  contain allOf(url1,url2,url3)
    }

  }
}