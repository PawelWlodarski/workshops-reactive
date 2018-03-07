package jug.workshops.poligon.typed

import java.util.concurrent.{ThreadLocalRandom, TimeUnit}

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior, SupervisorStrategy}

object Supervision {

  def main(args: Array[String]): Unit = {
    val root = Behaviors.setup[Nothing]{ctx =>
      val strategy=SupervisorStrategy.resume
      val runtimeSupervise = Behaviors.supervise(workerBehavior).onFailure[RuntimeException](strategy)
      val illegalStateSupervise= Behaviors.supervise(runtimeSupervise).onFailure[IllegalStateException](strategy)

      val worker=ctx.spawn(illegalStateSupervise, "worker")

      (1 to 20).foreach{n =>
        worker ! SupervisionJob(n.toString)

      }

      Behaviors.empty
    }

    val system=ActorSystem[Nothing](root,"sys")

    TimeUnit.SECONDS.sleep(3)
    system.terminate()

  }

  sealed trait SupervisionCommand
  final case class SupervisionJob(payload : String) extends SupervisionCommand

  val workerBehavior : Behavior[SupervisionCommand] = active(count=1)

  private def active(count:Int) : Behavior[SupervisionCommand] =
    Behaviors.immutable[SupervisionCommand]{(ctx,msg)=>
      msg match {
        case SupervisionJob(payload) =>
          if(ThreadLocalRandom.current().nextInt(5) == 0) throw new RuntimeException("bad luck")

          ctx.system.log.info("Worker {} got job{}, count {}",ctx.self,payload,count)
          active(count+1)
      }
    }
}
