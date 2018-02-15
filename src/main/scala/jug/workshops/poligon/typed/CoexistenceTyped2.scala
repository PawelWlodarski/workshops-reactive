package jug.workshops.poligon.typed

import java.util.concurrent.TimeUnit

import akka.actor.Props
import akka.actor.typed.scaladsl.Actor
import akka.actor.typed.{ActorRef, Behavior}

import akka.actor.typed.scaladsl.adapter._

object CoexistenceTyped2 {


  def main(args: Array[String]): Unit = {
    //exampleOne()
    exampleTwo()
  }


  private def exampleTwo(): Unit ={
    val system = akka.actor.ActorSystem("TypedWatchingUntyped")
    val typed: ActorRef[Typed2.Command2] = system.spawn(Typed2.behavior, "Typed")
    TimeUnit.SECONDS.sleep(3)
    system.terminate()
  }

  private def exampleOne() = {
    val system = akka.actor.ActorSystem("UntypedToTypedSystem")
    //    val typedSystem: ActorSystem[Nothing] = system.toTyped

    system.actorOf(Props[Untyped], "untypedFirst")
    TimeUnit.SECONDS.sleep(1)
    system.terminate()
  }

  object Typed {
    sealed trait Command
    final case class Ping(replyTo: ActorRef[Pong.type]) extends Command
    case object Pong


    val behavior: Behavior[Command] = Actor.immutable{ (ctx, msg) =>
      msg match {
        case Ping(replyTo) =>
          println(s"${ctx.self} got Ping from $replyTo")
          // replyTo is an untyped actor that has been converted for coexistence
          replyTo ! Pong
          Actor.same
      }
    }
  }

  class Untyped extends akka.actor.Actor{

    val second: ActorRef[Typed.Command] =context.spawn(Typed.behavior,"second")

    context.watch(second)

    second ! Typed.Ping(self)

    override def receive: Receive = {
      case Typed.Pong =>
        println(s"$self got Pong from ${sender()}")
        context.stop(second)
      case akka.actor.Terminated(ref) =>
        println(s"$self observed termination of $ref")
        context.stop(self)
    }
  }


  class Untyped2 extends akka.actor.Actor{
    override def receive: Receive = {
      case Typed2.Ping2(replyTo) =>
        println("received Ping2")
        replyTo ! Typed2.Pong2
    }
  }

  object Typed2 {
    final case class Ping2(replyTo : ActorRef[Command2])
    sealed trait Command2
    case object Pong2 extends Command2




    val behavior:Behavior[Command2] = Actor.deferred{ctx =>
      val untyped = ctx.actorOf(Props[Untyped2],"second2")
      ctx.watch(untyped)
      untyped.tell(Ping2(ctx.self),ctx.self.toUntyped)

      Actor.immutablePartial[Command2]{
        case (ctx,Pong2) =>
          println("stoping untyped")
          ctx.stop(untyped)
          Actor.same
      } onSignal {
        case (_, akka.actor.typed.Terminated(_)) =>
          println("stopping typed")
          Actor.stopped
      }
    }

  }

}
