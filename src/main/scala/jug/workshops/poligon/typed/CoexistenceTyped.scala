package jug.workshops.poligon.typed

import java.util.concurrent.TimeUnit

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, Terminated}


object CoexistenceTyped {

  import MyTyped1._
  import akka.actor.typed.scaladsl.adapter._

  def main(args: Array[String]): Unit = {
//    exampleOne()
    exampleTwo()
  }

  private def exampleOne() = {
    val oldSystem = akka.actor.ActorSystem("traditional")
    oldSystem.actorOf(MyUntyped1.props(), "first")
    TimeUnit.SECONDS.sleep(1)
    oldSystem.terminate()

  }

  private def exampleTwo() = {
    import akka.actor.typed.scaladsl.adapter._
    val system = akka.actor.ActorSystem("sys")
    system.spawn(MyTyped2.behavior, "first")
    TimeUnit.SECONDS.sleep(1)
    system.terminate()
  }

  object MyUntyped1 {
    def props():akka.actor.Props = akka.actor.Props(new MyUntyped1)
  }

  class MyUntyped1 extends akka.actor.Actor {

    val second: akka.actor.typed.ActorRef[Command] = context.spawn(MyTyped1.behavior, "second")

    context.watch(second)

    second ! MyTyped1.Ping(self)

    override def receive: Receive = {
      case MyTyped1.Pong =>  println(s"$self got Pong from ${sender()}")
        context.stop(second)
      case akka.actor.Terminated(ref) =>
        println(s"$self observed termination of $ref")
        context.stop(self)
    }
  }

  object MyTyped1 {

    sealed trait Command

    final case class Ping(replyTo: akka.actor.typed.ActorRef[Pong.type]) extends Command

    case object Pong

    val behavior: Behavior[Command] = akka.actor.typed.scaladsl.Behaviors.receive { (ctx, msg) =>
      msg match {
        case Ping(replyTo) =>
          println(s"${ctx.self} got Ping from $replyTo")
          replyTo ! Pong
          Behaviors.same
      }
    }

  }


  object MyTyped2 {
    final case class Ping2(replyTo: akka.actor.typed.ActorRef[Pong2.type])
    sealed trait Command2
    case object Pong2 extends Command2

    val behavior:Behavior[Command2] = Behaviors.setup{ctx =>
      val second=ctx.actorOf(MyUntyped2.props(),"second")

      ctx.watch(second)

      second.tell(Ping2(ctx.self),ctx.self.toUntyped)

      val behavior=Behaviors.receive[Command2]{ (ctx,msg) =>
        msg match {
          case Pong2 =>
            println(s"${ctx.self} got Pong2")
            ctx.stop(second)
            Behaviors.same
        }
      }

      behavior.receiveSignal{
        case (ctx, Terminated(ref)) =>
          println(s"${ctx.self} observed termination of $ref")
          Behaviors.stopped

        case sig =>
          println(sig)
          Behaviors.stopped
      }

      behavior
    }
  }

  object MyUntyped2{
    def props() = akka.actor.Props(new MyUntyped2)
  }

  class MyUntyped2 extends akka.actor.Actor {
    override def receive: Receive = {
      case MyTyped2.Ping2(replyTo) =>
        println(s"$self got Ping from ${sender()}")
        replyTo ! MyTyped2.Pong2
    }
  }


}
