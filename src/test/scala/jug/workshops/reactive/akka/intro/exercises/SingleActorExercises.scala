package jug.workshops.reactive.akka.intro.exercises

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit}
import jug.workshops.reactive.akka.intro.answers.ActorStack.Pop
import org.scalatest.{MustMatchers, WordSpecLike}

import scala.util.Success

class SingleActorExercises extends TestKit(ActorSystem("SingleActor")) with WordSpecLike with MustMatchers{

  //COMPLETE ACTOR CODE BELOW TESTS
  "EXERCISE1" should {
    "add all ints" in {
      val actor=TestActorRef[SimpleAccumulator]

      (1 to 10).par.foreach(actor ! _)

      actor.underlyingActor.state mustBe (1 to 10).sum
    }
  }

  //COMPLETE ACTOR CODE BELOW TESTS
  "EXERCISE2 - problem with generics" should {
    "merge given types according to strategy" in {
      val foldMultiply = (acc: Int, elem: Int) => acc * elem
      val foldSum = (acc: Int, elem: Int) => acc + elem

      val actorMultiply=TestActorRef[GenericAccumulator](GenericActor.props(1, foldMultiply),"MultiplyAccumulator")
      val actorSum=TestActorRef[GenericAccumulator](GenericActor.props(0, foldSum),"SumAccumulator")

      (1 to 5).par.foreach{i=>
        actorMultiply ! i
        actorSum ! i
      }

      actorMultiply.underlyingActor.state mustBe (1 to 5).reduce(foldMultiply)
      actorSum.underlyingActor.state mustBe (1 to 5).reduce(foldSum)

      //BONUS try to make actor really generic GenericAccumulator[A](empty:A, fold:(A,A)=>A)
    }
  }

  //COMPLETE ACTOR CODE BELOW TESTS
  "EXERCISE3 - filtering" should {
    "implement concurrent set" in {
      val actor=TestActorRef[ActorStack]

      //sequential execution
      Seq("one","two","three").map(ActorStack.Push.apply).foreach(actor ! _)

      actor.underlyingActor.orderedSet must contain only("one","two","three")

      import akka.pattern.ask
      import scala.concurrent.duration._
      import akka.util.Timeout
      implicit val timeout=Timeout(1 second)

      val Some(Success(elem1))=(actor ? Pop).value
      val Some(Success(elem2))=(actor ? Pop).value

      elem1 mustBe "three"
      elem2 mustBe "two"
    }
  }

}



class SimpleAccumulator extends Actor{

  var state:Int= ???

  override def receive: Receive = ???
}

class GenericAccumulator(empty:Int, fold:(Int,Int)=>Int) extends Actor{

  var state:Int = ???

  override def receive: Receive = ???
}

object GenericActor{
  def props(z:Int,fold:(Int,Int)=>Int) : Props = ???
}

import ActorStack._
class ActorStack extends Actor{

  var orderedSet = List.empty[String]

  override def receive: Receive = ???
}

object ActorStack{
  case class Push(i:String)
  case object Pop
}