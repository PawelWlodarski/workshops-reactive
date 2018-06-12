package jug.workshops.poligon.typed

import akka.NotUsed
import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, DeathPactException, SupervisorStrategy}
import akka.util.Timeout

import scala.concurrent.Await
import scala.util.{Failure, Success}
import akka.actor.typed.scaladsl.AskPattern._

import scala.concurrent.duration.FiniteDuration

object Poligon {

  def main(args: Array[String]): Unit = {
    import scala.concurrent.duration._

    val system: ActorSystem[NotUsed] = ???

    implicit val timeout: Timeout = 5 seconds
    implicit val scheduler = system.scheduler


    system.terminate()
    Await.result(system.whenTerminated, 3.seconds)


  }


  sealed trait Message
  case class Fail(text: String) extends Message

  val worker = Behaviors.receive[Message]{(ctx,msg) =>
    msg match {
      case Fail(text) => throw new RuntimeException(text)
    }
  }


  val middleManagementBehavior = Behaviors.setup[Message]{ctx =>
    ctx.log.info("Middle management starting up")
    val child = ctx.spawn(worker, "child")
    ctx.watch(child)


    Behaviors.receive[Message]{(ctx,message) =>
        child ! message
        Behaviors.same
    }
  }


  val bosBehavior = Behaviors.supervise(Behaviors.setup[Message]{ctx =>
    ctx.log.info("Boss starting up")
    val middleManagment = ctx.spawn(middleManagementBehavior, "middle-management")
    ctx.watch(middleManagment)

    Behaviors.receive[Message]{(ctx,message) =>
      middleManagment ! message
      Behaviors.same
    }

    ???
  }).onFailure[DeathPactException](SupervisorStrategy.restart)





}

