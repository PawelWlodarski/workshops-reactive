package jug.workshops.poligon.typed

import java.util.concurrent.TimeUnit

import akka.actor.typed.receptionist.Receptionist.{Listing, Subscribe}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}

object DiscoveryTyped {

  val PingServiceKey = ServiceKey[DiscoveryPing]("pingService")

  final case class DiscoveryPing(replyTo : ActorRef[DiscoveryPong.type])
  final case object DiscoveryPong


  val pingService : Behavior[DiscoveryPing] = Behaviors.setup{ ctx =>
    ctx.system.receptionist ! Receptionist.Register(PingServiceKey,ctx.self, ctx.system.deadLetters)
    Behaviors.immutable[DiscoveryPing]{ (_, msg) =>
      msg match {
        case DiscoveryPing(replyTo) =>
          replyTo ! DiscoveryPong
          Behaviors.stopped

      }
    }
  }


  def pinger(pingService : ActorRef[DiscoveryPing]) = Behaviors.setup[DiscoveryPong.type ]{ctx =>
    pingService ! DiscoveryPing(ctx.self)
    Behaviors.immutable{(_,msg) =>
      println("I was ponged!!" + msg)
      Behaviors.same
    }
  }

  val guardian : Behavior[Listing] = Behaviors.setup{ctx =>
    ctx.system.receptionist ! Subscribe(PingServiceKey,ctx.self)
    val ps = ctx.spawnAnonymous(pingService)
    ctx.watch(ps)

    Behaviors.immutablePartial[Listing]{
      case (c, PingServiceKey.Listing(listings)) if listings.nonEmpty =>
        listings.foreach(ps => ctx.spawnAnonymous(pinger(ps)))
        Behaviors.same
    } onSignal {
      case (_,Terminated(`ps`)) =>
        println("Ping service has shut down")
        Behaviors.stopped
    }

  }


  def main(args: Array[String]): Unit = {
    val system = ActorSystem(guardian,"discovering")

    TimeUnit.SECONDS.sleep(3)
    system.terminate()
  }


}
