package jug.workshops.poligon.typed

import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}
import akka.actor.typed.{ActorRef, Behavior}

import scala.concurrent.duration.FiniteDuration

object Timers {

  trait TimersMsg
  final case class Batch(messages : Vector[TimersMsg])

  private case object TimerKey
  private case object Timeout extends TimersMsg


  private def idle(timers: TimerScheduler[TimersMsg], target: ActorRef[Batch],after:FiniteDuration,maxSize:Int)
  : Behavior[TimersMsg] = Behaviors.immutable[TimersMsg]{ (ctx,msg) =>
      timers.startSingleTimer(TimerKey,Timeout,after)
      active(Vector(msg),timers,target,after,maxSize)
  }


  private def active(buffer : Vector[TimersMsg], timers: TimerScheduler[TimersMsg],
                     target: ActorRef[Batch], after: FiniteDuration, maxSize:Int):Behavior[TimersMsg]=
    Behaviors.immutable[TimersMsg]{(ctx,msg) =>
      msg match {
        case Timeout =>
          target ! Batch(buffer)
          idle(timers,target,after,maxSize)
        case msg => ???
      }


    ???
  }



}
