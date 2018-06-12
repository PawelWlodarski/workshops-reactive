package jug.workshops.poligon.typed.routers

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object Worker {
  sealed trait WorkerCommand
  final case class Job(payload:String) extends WorkerCommand

  val workerBehavior:Behavior[WorkerCommand] =
    Behaviors.receive[WorkerCommand]{(ctx,msg) =>
      msg match {
        case Job(payload) =>
          println(s"Worker ${ctx.self} got job $payload")
          Behaviors.same
      }

    }
}
