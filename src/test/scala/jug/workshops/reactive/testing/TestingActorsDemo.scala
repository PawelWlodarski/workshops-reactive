package jug.workshops.reactive.testing

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.TestActor.AutoPilot
import akka.testkit.{ImplicitSender, TestActor, TestActorRef, TestKit, TestProbe}
import akka.util.Timeout
import common.StopSystemAfterAll
import org.scalatest.{MustMatchers, WordSpecLike}

import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.Success

/**
  * Created by pawel on 16.10.16.
  */
class TestingActorsDemo extends TestKit(ActorSystem("testing-demo")) with WordSpecLike with StopSystemAfterAll
  with MustMatchers with ImplicitSender {

  //What is TestKit
  //What is StopsystemAfter all
  //What is implicitSender

  "Synchronous test" should {

    "create TestActorRef" in {
      val sut = TestActorRef[ActorWithState]

      sut ! "one"
      sut ! "two"
      sut ! "three"

      sut.underlyingActor.state mustBe 3
    }

    "get asynchronously state" in {
      //given
      val sut = TestActorRef[ActorWithState]

      sut ! "one"
      sut ! "two"
      sut ! "three"

      //when
      import akka.pattern.ask

      import scala.concurrent.duration._
      implicit val timeout = Timeout(1 second)
      val request = sut ? "getState"

      //then
      // EXPLAIN PATTERN MATCHING
      // EXPLAIN WHY mapTo works here immediately
      val Success(state) = request.mapTo[Int].value.get

      state mustBe 3
    }
  }

  "Asynchronous test" should {
    "use implicit sender" in {
      val sut = system.actorOf(Props[ActorWithState])

      sut ! "one"
      sut ! "two"
      sut ! "three"

      sut ! "getState"

      //EXPLAIN ASYNC ASSERTION
      expectMsgPF() {
        case state: Int => state mustBe 3
      }
    }


    "use receive while" in {
      val sutProbe = TestProbe()
      val sut = sutProbe.ref

      sut ! "one"
      sut ! "two"
      sut ! "three"
      sut ! "four"
      sut ! "five"

      import scala.concurrent.duration._
      val messages=sutProbe.receiveWhile(500 millis){
        case message:String if message != "four" => message
      }

      messages must contain only("one","two","three")

    }

    "use receiveM " in {
      val sutProbe = TestProbe()
      val sut = sutProbe.ref

      sut ! "one"
      sut ! "two"
      sut ! "three"
      sut ! "four"
      sut ! "five"

      import scala.concurrent.duration._
      val messages: Seq[String] =sutProbe.receiveN(4,1 second).map(_.toString)

      messages must contain only("one","two","three","four")

    }

    "expect no messages" in {
      val probe = TestProbe()

      probe.ref ! "one"
      probe.ref ! "two"
      probe.ref ! "three"

      probe.expectMsgAllOf("one","two","three")
      TestProbe().expectNoMsg(Duration(200,TimeUnit.MILLISECONDS)) // WHEN USEFUL ?
    }
  }

  "Test Probe" should {
    "run with auto pilot" in {

      val probe1=TestProbe()
      val probe2=TestProbe()

      probe1.setAutoPilot((_ , msg: Any) => {
        probe2.ref.tell(msg, testActor)
        TestActor.KeepRunning
      })

      //SCALA 2.12 !!!!
      probe2.setAutoPilot(new AutoPilot {
        override def run(sender: ActorRef, msg: Any): AutoPilot = {
          sender ! msg
          TestActor.KeepRunning
        }
      })

      probe1.ref ! "one"
      probe1.ref ! "two"
      probe1.ref ! "three"

      probe2.ref ! "four"
      probe2.ref ! "five"
      probe2.ref ! "six"


      val responses: Seq[AnyRef] =receiveN(6)

      println(responses)

      responses must have size(6)

    }

    "fish for message" in {
      val probe1=TestProbe()

      probe1.ref ! "one"
      probe1.ref ! 22
      probe1.ref ! "three"

      probe1.ref ! "four"
      probe1.ref ! 77
      probe1.ref ! "six"

      val r: Any =probe1.fishForMessage(){
        case m:Int if m> 50 => true
        case _ => false
      }

      r mustBe 77
    }

    "await for condition" in {
      var closure = 0
      import scala.concurrent.ExecutionContext.Implicits.global
      Future{
        //Don't do this on production!!!
        TimeUnit.SECONDS.sleep(1)
        println("changing closure to 1")
        closure=1
      }

      println("start waiting for condition")
      awaitCond(
        p = closure>0,
        max = Duration(2,TimeUnit.SECONDS),
        interval = Duration(100,TimeUnit.MILLISECONDS))

      println("after condition")

      //awaitAssert
    }
  }
}

class ActorWithState extends Actor {

  var state = 0

  override def receive: Receive = {
    case "getState" => sender ! state
    case _ => state = state + 1
  }
}
