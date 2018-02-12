package jug.workshops.poligon.typed.routers

import akka.typed.Behavior
import akka.typed.scaladsl.Actor

object Worker {
  sealed trait WorkerCommand
  final case class Job(payload:String) extends WorkerCommand

  val workerBehavior:Behavior[WorkerCommand] =
    Actor.immutable[WorkerCommand]{(ctx,msg) =>
      msg match {
        case Job(payload) =>
          println(s"Worker ${ctx.self} got job $payload")
          Actor.same
      }

    }
}
