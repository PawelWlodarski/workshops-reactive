package jug.workshops.reactive.akka.routing.exercises

import akka.actor.{ActorSystem, Props}
import akka.routing.ConsistentHashingPool
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import common.StopSystemAfterAll
import jug.workshops.reactive.akka.routing.exercises.RoutersPart3ProtocolTransmissionExercise._
import org.scalatest.{MustMatchers, WordSpecLike}

class RoutersPart3ProtocolTransmissionExerciseSpec extends TestKit(ActorSystem()) with MustMatchers
    with WordSpecLike with StopSystemAfterAll with ImplicitSender{

  //you can switch from ConsistentHashableEnvelope to ConsistentHashable trait if you want
  "Transmitter" should {
    "split message into bytes" in {
      val probe=TestProbe()

      val transmitter=system.actorOf(Props(new Transmitter(probe.ref,bandStart = 1)))

      transmitter ! TransmissionWord(List(One,Zero,Zero,One))

      import scala.concurrent.duration._
      val bytes=probe.receiveWhile(500 millis){
        case ConsistentHashableEnvelope(Transmission(_,byte),_) => byte
      }

      bytes mustBe List(One,Zero,Zero,One)

    }
  }

  "Receiver" should {
    "translate bits into string message" in {
      val probe=TestProbe()

      val receiver=system.actorOf(Props(new TransmissionReceiver(probe.ref)))

      receiver ! Transmission(1,One)
      receiver ! Transmission(1,Zero)
      receiver ! Transmission(1,Zero)
      receiver ! Transmission(1,One)

      probe.expectMsg("1001")

    }
  }


  "Integration Test" should {
    "Route messages between sender and receiver" in {
      val probe=TestProbe()
      val receiverRouter=system.actorOf(ConsistentHashingPool(3).props(TransmissionReceiver.props(probe.ref)))
      val transmitter1=system.actorOf(Props(new Transmitter(receiverRouter,bandStart = 1)))
      val transmitter2=system.actorOf(Props(new Transmitter(receiverRouter,bandStart = 100)))
      val transmitter3=system.actorOf(Props(new Transmitter(receiverRouter,bandStart = 200)))


      transmitter1 ! TransmissionWord(List(One,Zero,Zero,Zero))
      transmitter2 ! TransmissionWord(List(One,One,One,One))
      transmitter3 ! TransmissionWord(List(Zero,Zero,One,One))

      import scala.concurrent.duration._
      probe.expectMsgAllOf(1 second,"1000","1111","0011")
    }
  }

}
