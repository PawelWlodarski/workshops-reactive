package jug.workshops.reactive.testing

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.TestActor.AutoPilot
import akka.testkit.{ImplicitSender, TestActor, TestKit, TestProbe}
import common.StopSystemAfterAll
import jug.workshops.reactive.testing.BrowserForTesting._
import org.scalatest._

import scala.collection.immutable.Seq

/**
  * Created by pawel on 16.10.16.
  */
class AsynchronousTestingAnswer extends TestKit(ActorSystem("AsyncBrowser")) with WordSpecLike
  with StopSystemAfterAll with MustMatchers with ImplicitSender{

  "browser" should {

    //INTERFACE
    "store internet calls in history" in{
      val internet=TestProbe()
      val browser = system.actorOf(Props(new BrowserForTesting(internet.ref)))

      browser ! Call(Url("test1"))
      browser ! Call(Url("test2"))

      //when
      browser ! RequestForHistory

      expectMsgPF(){
        case history:Seq[_] => history must contain allOf(Url("test1"),Url("test2"))  //uwaga na brak allOf!!!
      }

    }

    "not store anonymous calls in history" in{
      val internet=TestProbe()
      val browser = system.actorOf(Props(new BrowserForTesting(internet.ref)))

      browser ! Call(Url("test1"))
      browser ! AnonymousCall(Url("test2"))
      browser ! Call(Url("test3"))

      //when
      browser ! RequestForHistory

      expectMsgPF(){
        case history:Seq[_] => history must contain only (Url("test1"),Url("test3"))
      }
    }

    //TEST FIXTURE !!!
    trait WithBrowser{
      val internet=TestProbe()
      val browser = system.actorOf(Props(new BrowserForTesting(internet.ref)),"browser")
    }

    "return 'NotFound' page when wrong type is send" in new WithBrowser{
        browser ! "UnknownMessage"

        expectMsgPF(){
          case Response(code,_) =>
            code mustBe 500
        }
    }


    //COMMUNICATION
    "should filter out messages with wrong type (varian1)" in new WithBrowser{
      browser ! Call(Url("test1"))
      browser ! Call(Url("test2"))
      browser ! Call(Url("test3"))
      browser ! "Wrong"
      browser ! 42
      browser ! List(69,96)


      val urls=internet.receiveWhile(){
        case Call(Url(url)) => url
      }


      urls must contain only("test1","test2","test3")

      import scala.concurrent.duration._
      internet.expectNoMsg(500 millis)  // should this timeout be larger than the one below?

      expectMsgPF(1000 millis){
        case Response(code,_) =>
          code mustBe 500
      }

    }


    "should filter out messages with wrong type (varian2)" in new WithBrowser{
      //given
      internet.setAutoPilot(new AutoPilot {
        override def run(sender: ActorRef, msg: Any): AutoPilot ={
          sender ! Response(200,Page(""))
          TestActor.KeepRunning
        }
      })

      //when
      browser ! Call(Url("test1"))
      browser ! Call(Url("test2"))
      browser ! AnonymousCall(Url("test3"))
      browser ! "Wrong"
      browser ! 42
      browser ! List(69,96)
      browser ! Call(Url("test3"))


      import scala.concurrent.duration._
      internet.receiveN(4,500 millis)
      val clientMessages=receiveN(7,1 second)                  //implicit actor

      clientMessages.collect{case Response(code,body) => code} mustBe Seq(200,200,200,500,500,500,200)
    }

  }
}





