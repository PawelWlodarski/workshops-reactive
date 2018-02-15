package jug.workshops.poligon.typed

import java.util.concurrent.TimeUnit

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import akka.actor.typed.receptionist.Receptionist.{Listing, Subscribe}
import akka.actor.typed.scaladsl.Actor

object DiscoveryTyped {

  val PingServiceKey = ServiceKey[DiscoveryPing]("pingService")

  final case class DiscoveryPing(replyTo : ActorRef[DiscoveryPong.type])
  final case object DiscoveryPong


  val pingService : Behavior[DiscoveryPing] = Actor.deferred{ ctx =>
    ctx.system.receptionist ! Receptionist.Register(PingServiceKey,ctx.self, ctx.system.deadLetters)
    Actor.immutable[DiscoveryPing]{ (_, msg) =>
      msg match {
        case DiscoveryPing(replyTo) =>
          replyTo ! DiscoveryPong
          Actor.stopped

      }
    }
  }


  def pinger(pingService : ActorRef[DiscoveryPing]) = Actor.deferred[DiscoveryPong.type ]{ctx =>
    pingService ! DiscoveryPing(ctx.self)
    Actor.immutable{(_,msg) =>
      println("I was ponged!!" + msg)
      Actor.same
    }
  }

  val guardian : Behavior[Listing[DiscoveryPing]] = Actor.deferred{ctx =>
    ctx.system.receptionist ! Subscribe(PingServiceKey,ctx.self)
    val ps = ctx.spawnAnonymous(pingService)
    ctx.watch(ps)

    Actor.immutablePartial[Listing[DiscoveryPing]]{
      case (c, Listing(PingServiceKey,listings)) if listings.nonEmpty =>
        listings.foreach(ps => ctx.spawnAnonymous(pinger(ps)))
        Actor.same
    } onSignal {
      case (_,Terminated(`ps`)) =>
        println("Ping service has shut down")
        Actor.stopped
    }

  }


  def main(args: Array[String]): Unit = {
    val system = ActorSystem(guardian,"discovering")

    TimeUnit.SECONDS.sleep(3)
    system.terminate()
  }


}
