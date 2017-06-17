package jug.workshops.reactive.akka.intro

import akka.actor.{Actor, ActorSystem}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{MustMatchers, WordSpecLike}
import SingleRespondingActor._

class SingleActorAsyncTest extends TestKit(ActorSystem("test")) with  WordSpecLike with MustMatchers with ImplicitSender{

  //EXPLAIN IMPLICIT SENDER
  "SingleActor" must {
    "accumulate state" in {
      val actorRef=TestActorRef[SingleRespondingActor]
      println("testThread : "+Thread.currentThread().getName)
      actorRef ! Push(1)
      actorRef ! Push(2)
      actorRef ! Push(3)
      actorRef ! Push(4)

      actorRef  ! Pop
      expectMsg(Result(4))  //ASYNC MATCHER
      actorRef  ! Pop
      expectMsg(Result(3))  //SHOW WRONG ELEMENT MATCHING ERROR
//      expectMsg(Result(2))  //SHOW TIMEOUt ERROR
    }
  }
}



class SingleRespondingActor extends Actor{
  var state=List.empty[Int]

  override def receive: Receive = {
    case Push(i) =>
      state = i :: state
    case Pop =>
      val result=Result(state.head)  //not care about exceptions in this exercise
      state = state.tail
      sender() ! result //explain sender!
  }
}

object SingleRespondingActor {
  case class Push(elem:Int)
  case object Pop
  case class Result(elem:Int)
}
