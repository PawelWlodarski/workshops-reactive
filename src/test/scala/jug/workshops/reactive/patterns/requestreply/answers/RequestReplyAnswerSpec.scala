package jug.workshops.reactive.patterns.requestreply.answers

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
      val node=system.actorOf(Props[SimpleNode])

      node ! Sum((1.0 to 5.0 by 1))

      expectMsgPF(){
        case Response(body) => body mustBe 15.0
      }

      node ! SquareRoot(25.0)

      expectMsgPF(){
        case Response(body) => body mustBe 5.0
      }

    }
  }

}
