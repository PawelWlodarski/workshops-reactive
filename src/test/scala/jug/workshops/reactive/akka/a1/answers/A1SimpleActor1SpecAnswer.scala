package jug.workshops.reactive.akka.a1.answers

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import common.StopSystemAfterAll
import org.scalatest.{MustMatchers, WordSpecLike}

/**
  * Created by pawel on 18.10.16.
  */
class A1SimpleActor1SpecAnswer extends TestKit(ActorSystem("SimpleActor")) with WordSpecLike
  with MustMatchers with StopSystemAfterAll with ImplicitSender {

  "SimpleActorForTestingAnswer" should {
    "return true if 'i' is Even and false if 'i' is Odd" in {
      //given
      val actorUnderTesting = system.actorOf(Props[SimpleActorForTestingAnswer])

      val even = 20
      val odd = 21

      //when
      actorUnderTesting ! even

      //then
      expectMsg(true)

      //when
      actorUnderTesting ! odd

      //then
      expectMsg(false)
    }

    "return Sum of tuple" in {
      //given
      val actorUnderTesting = system.actorOf(Props[SimpleActorForTestingAnswer])

      val t1 = (3, 7)
      val t2 = (-2, 5)

      //when
      actorUnderTesting ! t1
      actorUnderTesting ! t2

      //then
      expectMsg(10)
      expectMsg(3)
    }
  }

  "Multiplying calculator" should {
    "multiply elements of received tuple : (i1,i2) => i1*i2" in {
      //given
      val calc = system.actorOf(Props[MultiplyingCalculator])

      //when
      calc ! (3, 5)
      calc ! (2, 3)

      //then
      expectMsg(15)
      expectMsg(6)
    }
  }
}


class SimpleActorForTestingAnswer extends Actor {
  override def receive: Receive = {
    case "one" => println("in actor : received one")
    case "two" => println("in actor : received two")
    case i: Int => sender ! (i % 2 == 0)
    case (i: Int, j: Int) => sender ! i + j
    case msg => println(s"in actor : received unknown message : [value=$msg, type=${msg.getClass} ]")
  }
}

class MultiplyingCalculator extends Actor {
  override def receive: Receive = {
    case (i: Int, j: Int) => sender ! i * j
  }
}