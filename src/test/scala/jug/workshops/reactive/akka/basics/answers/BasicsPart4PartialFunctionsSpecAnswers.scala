package jug.workshops.reactive.akka.basics.answers

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import common.StopSystemAfterAll
import org.scalatest.{MustMatchers, WordSpecLike}

/**
  * Created by pawel on 30.10.16.
  */

class BasicsPart4PartialFunctionsSpecAnswers extends TestKit(ActorSystem("test")) with MustMatchers
  with WordSpecLike with StopSystemAfterAll with ImplicitSender {


  "PartialFunctionCalculator" should {
    "handle addition and multiplication" in {
      lazy val add: PartialFunction[(Int, Int, String), Int] = {
        case (a, b, "+") => a + b
      }

      lazy val mult: PartialFunction[(Int, Int, String), Int] = {
        case (a, b, "*") => a * b
      }

      val calc = add orElse mult

      calc(1, 2, "+") mustBe 3
      calc(6, 2, "*") mustBe 12
      calc(6, 3, "*") mustBe 18

    }
  }


  "Calculator with state" should {
    "preserve operations history" in {
      import ObjectWithState._

      val encapsulatedState=new ObjectWithState()
      val testProbe=new Sender

      encapsulatedState.receive(Add(2))
      encapsulatedState.receive(Add(3))
      encapsulatedState.receive(PrintState(testProbe))
      encapsulatedState.receive(Mult(6))
      encapsulatedState.receive(PrintState(testProbe))

      testProbe.states must contain only(5,30)
    }
  }

  object ObjectWithState{
    case class Add(v:Int)
    case class Mult(v:Int)
    case class PrintState(sender:Sender)
  }

  class ObjectWithState{
    import ObjectWithState._
    type Receive = PartialFunction[Any, Unit]

    private var state:Int=0

    val receive:Receive={
      case Add(n) => state = state + n
      case Mult(n) => state = state * n
      case PrintState(sender) => sender.addState(state)
    }
  }

  class Sender {
    //don't give access to var
    private var statesInternal=Vector[Int]()
    def addState(newState:Int) = statesInternal = statesInternal :+ newState
    def states = statesInternal

  }

}
