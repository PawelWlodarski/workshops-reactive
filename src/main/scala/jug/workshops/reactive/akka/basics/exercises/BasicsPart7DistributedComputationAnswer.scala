package jug.workshops.reactive.akka.basics.exercises

import akka.actor.{Actor, ActorRef}

import scala.annotation.tailrec


object BasicsPart7DistributedComputationAnswer {

  import JobDivisionProtocol._

  object JobDivisionProtocol {

    case class ComputationTask(start: Long, end: Long)

    case class Result(v: BigDecimal)

    case class SendResponse(probe: ActorRef)

  }

  object ProcessingLogic {
    def computation(start: Long, end: Long) : BigDecimal = {

      @tailrec
      def sum(v: Long, acc: BigDecimal): BigDecimal = v match {
        case `end` => acc
        case _ => sum(v + 1, acc + v)
      }

      sum(start, BigDecimal(0))
    }
  }

  class TaskProcessor(combiner: ActorRef) extends Actor {
    override def receive: Receive = {
      case c@ComputationTask(start, end) =>
        val result = ProcessingLogic.computation(start, end)
        combiner ! Result(result)
    }
  }


  class Combiner extends Actor {

    var result = BigDecimal(0)

    override def receive: Actor.Receive = {
      case Result(v) => result = result + v
      case SendResponse(probe) => probe ! result
    }
  }

}