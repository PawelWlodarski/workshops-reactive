package jug.workshops.reactive.akka.typed

import java.util.concurrent.TimeUnit

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object Coexistence {

  import akka.actor.typed.scaladsl.adapter._

  def main(args: Array[String]): Unit = {
    typedToUntyped()
  }


  def typedToUntyped() = {
    val system = akka.actor.ActorSystem("TypedToUntypedSystem")

    val untypedReceiver=system.actorOf(akka.actor.Props[OldActorReceiver],"UntypedActor")

    system.spawn(typedSender(untypedReceiver),"untypedReceiver")

    TimeUnit.SECONDS.sleep(1)
    system.terminate()
  }

  sealed trait TypedToUntyped
  case class RequestFromTyped(replyTo:akka.actor.ActorRef) extends TypedToUntyped
  case class ResponseFromUntyped(replyTo:akka.actor.typed.ActorRef[String]) extends TypedToUntyped

  class OldActorReceiver extends akka.actor.Actor{
    override def receive: Receive = {
      case RequestFromTyped(replyTo) =>
        println("received request from typed")
        replyTo ! ResponseFromUntyped(self)
    }
  }

  def typedSender(receiver:akka.actor.ActorRef): Behavior[ResponseFromUntyped] =
    Behaviors.setup{ctx =>

      receiver ! RequestFromTyped(ctx.self.toUntyped)

      Behaviors.receive[ResponseFromUntyped]{(_,msg) =>
        println(s"received $msg back")
        Behaviors.same
      }
    }


}
