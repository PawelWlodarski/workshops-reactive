package jug.workshops.reactive.patterns.requestreply.answers

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import common.StopSystemAfterAll
import jug.workshops.reactive.patterns.requestreply.answers.SimpleNode.{Response, SquareRoot, Sum}
import org.scalatest.{MustMatchers, WordSpecLike}

/**
  * Created by pawel on 09.10.16.
  */
class RequestReplyAnswerSpec extends TestKit(ActorSystem("testingRequestReply")) with WordSpecLike
  with MustMatchers with ImplicitSender with StopSystemAfterAll{

  "Request-Reply" should {
    "receive responses from simple node" in {
      val node=system.actorOf(Props[SimpleNode],"simpleNode")

      node ! Sum((1.0 to 5.0 by 1))

      expectMsgPF(){
        case Response(body) => body mustBe 15.0
      }

      node ! SquareRoot(25.0)

      expectMsgPF(){
        case Response(body) => body mustBe 5.0
      }

    }

    "receive responses from delegate node (how to test async?)" in {
      import scala.concurrent.duration._

      val node=system.actorOf(Props[DelegateNode],"delegateNode")

      node ! Sum((1.0 to 5.0 by 1))
      TimeUnit.MILLISECONDS.sleep(100)
      node ! SquareRoot(25.0)
      TimeUnit.MILLISECONDS.sleep(100)
      node ! SquareRoot(36.0)

      expectMsgPF(max = 600 millis){
        case Response(body) => body mustBe 15.0
      }

      expectMsgPF(max = 600 millis){
        case Response(body) => body mustBe 5.0
      }

      expectMsgPF(max = 600 millis){
        case Response(body) => body mustBe 6.0
      }

    }


    "receive responses from delegate variant2" in {
      import scala.concurrent.duration._

      val node=system.actorOf(Props[DelegateNode],"delegateNode2")

      node ! Sum((1.0 to 5.0 by 1))
      node ! SquareRoot(25.0)
      node ! SquareRoot(36.0)

      expectMsgAnyOf(max = 600 millis,Response(15.0),Response(5.0),Response(6.0))
      expectMsgAnyOf(max = 600 millis,Response(15.0),Response(5.0),Response(6.0))
      expectMsgAnyOf(max = 600 millis,Response(15.0),Response(5.0),Response(6.0))
    }
  }

}
