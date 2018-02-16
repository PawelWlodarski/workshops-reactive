package jug.workshops.poligon.typed

import java.io.{FileWriter, PrintWriter}
import java.util.concurrent.ThreadLocalRandom

import akka.actor.typed.{Behavior, PostStop, PreRestart}
import akka.actor.typed.scaladsl.Actor

object Lifecycle {


  sealed trait LifecycleCommand
  final case class LifecycleJob(payload: String) extends LifecycleCommand

  val workerBehavior: Behavior[LifecycleCommand] = Actor.deferred[LifecycleCommand]{ ctx =>
    ctx.system.log.info("Worker {} is STARTED", ctx.self)
    val out = new PrintWriter(new FileWriter(s"target/out-${ctx.self.path.name}.txt", true))
    active(count=1, out)
  }

  private def active(count:Int, out:PrintWriter): Behavior[LifecycleCommand] = Actor.immutable[LifecycleCommand] {
    (ctx,msg) =>
      msg match {
        case LifecycleJob(payload) =>
          if(ThreadLocalRandom.current().nextInt(5) == 0) throw new RuntimeException("Bad Luck")

          ctx.system.log.info("Worker {} got job {}, count {}" , ctx.self , payload , count)
          out.println(s"Worker ${ctx.self} got job $payload, count $count")
          active(count + 1, out)
      }

  } onSignal {
    case (ctx,PreRestart) =>
      ctx.system.log.info("Worker {} is RESTARTED, count {} ", ctx.self, count)
      out.close()
      Actor.same
    case (ctx, PostStop) =>
      ctx.system.log.info("Worker {} is STOPPED, count {}", ctx.self, count)
      out.close()
      Actor.same
  }

}
