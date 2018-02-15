package jug.workshops.poligon.typed

import akka.actor.typed.scaladsl.Actor
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}

import scala.concurrent.Await

object ChatRoom {

  sealed trait Command

  final case class GetSession(screenName: String, replyTo: ActorRef[SessionEvent]) extends Command

  private final case class PostSessionMessage(screenName: String, message: String) extends Command

  sealed trait SessionEvent

  final case class SessionGranted(handle: ActorRef[PostMessage]) extends SessionEvent

  final case class SessionDenied(reason: String) extends SessionEvent

  final case class MessagePosted(screenName: String, message: String) extends SessionEvent

  final case class PostMessage(message: String)

  val behavior:Behavior[Command] = chatRoom(List.empty)

  private def chatRoom(sessions: List[ActorRef[SessionEvent]]): Behavior[Command] =
    Actor.immutable[Command] { (ctx, msg) =>
      msg match {
        case GetSession(screenName, client) =>
          val wrapper = ctx.spawnAdapter {
            p: PostMessage => PostSessionMessage(screenName, p.message)
          }
          client ! SessionGranted(wrapper)
          chatRoom(client :: sessions)
        case PostSessionMessage(screenName, message) =>
          val mp = MessagePosted(screenName,message)
          sessions foreach (_ ! mp)
          Actor.same
      }
    }

  val gabbler = Actor.immutable[SessionEvent]{ (_,msg) =>
    msg match {
      case SessionGranted(handle) =>
        handle ! PostMessage("Hello World!")
        Actor.same
      case MessagePosted(screenName,message) =>
        println(s"message has been posted by '$screenName': $message")
        Actor.stopped
      case unsupported => throw new RuntimeException(s"received $unsupported")
    }
  }

  def main(args: Array[String]): Unit = {

    val root: Behavior[String] =Actor.deferred{ ctx =>
      val chatroom: ActorRef[Command] =ctx.spawn(behavior,"chatroom")
      val gabblerRef: ActorRef[SessionEvent] =ctx.spawn(gabbler,"gabbler")
      ctx.watch(gabblerRef)

      Actor.immutablePartial[String]{
        case (_, "go") =>
          chatroom ! GetSession("Gabber",gabblerRef)
          Actor.same
      }.onSignal{
        case (_,Terminated(ref)) =>
          println(s"$ref is terminated")
          Actor.stopped
      }
    }


    val system = ActorSystem(root, "chatroom")
    system ! "go"

    import scala.concurrent.duration._
    Await.result(system.whenTerminated, 3.seconds)

  }

}
