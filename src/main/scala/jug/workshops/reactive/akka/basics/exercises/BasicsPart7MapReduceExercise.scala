package jug.workshops.reactive.akka.basics.exercises

import akka.actor.{Actor, ActorRef}


object BasicsPart7MapReduceExercise {

  import JobDivisionProtocol._

  object JobDivisionProtocol {

    case class ComputationTask(start: Long, end: Long)

    case class Result(v: BigDecimal)

    case class SendResponse(probe: ActorRef)

  }

  object ProcessingLogic {
    //compute sum in most convenient way
    //check if it easier to test logic outside actors
    def computation(start: Long, end: Long) = ???
  }

  class TaskProcessor(combiner: ActorRef) extends Actor {
    override def receive: Receive = {
      case ComputationTask(start, end) => ??? //calculate sum and pass to combiner
    }
  }


  class Combiner extends Actor {

    var result = ???

    override def receive: Actor.Receive = {
      case Result(v) => ???
      case SendResponse(probe) => ???
    }
  }

}