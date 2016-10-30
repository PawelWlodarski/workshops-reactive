package jug.workshops.reactive.akka.basics.exercises

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import common.StopSystemAfterAll
import jug.workshops.reactive.akka.basics.exercises.DemoActorProtocol.{DemoMessage, DemoResponse}
import org.scalatest.{MustMatchers, WordSpecLike}

/**
  * Created by pawel on 30.10.16.
  */
class BasicsPart6Demo extends TestKit(ActorSystem("testing")) with MustMatchers
    with WordSpecLike with StopSystemAfterAll with ImplicitSender{

    "Demo actor" should {
      "send expected responses" in {
        val other=TestProbe()

        val demoActor=system.actorOf(Props(new DemoPart6Actor(other.ref)))

        demoActor ! DemoMessage("aa")
        demoActor ! DemoMessage("bbb")
        demoActor ! DemoMessage("cccc")
        demoActor ! DemoMessage("cccc")
        demoActor ! DemoMessage("cccc")
        demoActor ! DemoMessage("ddddd")

        other.expectMsg(DemoResponse(2))
        other.expectMsgPF(){
          case DemoResponse(number) => number mustBe 3
        }

        import scala.concurrent.duration._

        val receivedNumbers=other.receiveWhile(500 millis){
          case DemoResponse(number) => number
        }

        receivedNumbers must contain theSameElementsInOrderAs(List(4,4,4,5))

        other.expectNoMsg(500 millis)

      }
    }

}

object DemoActorProtocol{
  case class DemoMessage(text:String)
  case class DemoResponse(length:Int)

}

import jug.workshops.reactive.akka.basics.exercises.DemoActorProtocol._

class DemoPart6Actor(other:ActorRef) extends Actor{
  override def receive: Receive = {
    case demo @ DemoMessage(text) => other ! DemoResponse(text.length)
  }
}


