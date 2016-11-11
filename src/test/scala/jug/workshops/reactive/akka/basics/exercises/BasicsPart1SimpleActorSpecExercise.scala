package jug.workshops.reactive.akka.basics.exercises

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import common.StopSystemAfterAll
import org.scalatest.{MustMatchers, WordSpecLike}

/**
  * Created by pawel on 18.10.16.
  */
class A1SimpleActor1SpecExercise extends TestKit(ActorSystem("SimpleActor")) with WordSpecLike
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

  //Finish Implementation of MultiplyingCalculator prepared at the bottom of this file
  "Multiplying calculator" should {
    "multiply elements of received tuple : (i1,i2) => i1*i2" in {
      //given
      val calc:ActorRef = ???

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
    case "two" =>
      println("sending message two to sender")
      sender ! "in actor : received two"
    case i: Int => ???
//    case ??? => ??? ///match on tuple (Int,Int)
    case msg => println(s"in actor : received unknown message : [value=$msg, type=${msg.getClass} ]")
  }
}

//turn this class into actor to finish exercise 3
class MultiplyingCalculator  {
}