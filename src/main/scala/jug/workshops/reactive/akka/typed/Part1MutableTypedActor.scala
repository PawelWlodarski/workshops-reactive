package jug.workshops.reactive.akka.typed

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.Behaviors.MutableBehavior
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

import scala.concurrent.Await


object Part1MutableTypedActor {

  def main(args: Array[String]): Unit = {
    //everything in package akka.actor.typed or akka.actor.typed.scaladsl (since 2.5.11)
    val behavior = akka.actor.typed.scaladsl.Behaviors.mutable[Part1Command](ctx => new MutableTyped())

    //main thread
    println(s"starting in thread ${Thread.currentThread().getName}")

    //special setup behavior
    val main: Behavior[NotUsed] =Behaviors.setup{ ctx =>
      //some thread from actor pool
      println(s"setup in thread ${Thread.currentThread().getName}")
      val helloActor: ActorRef[Part1Command] =ctx.spawn(behavior,"HelloActor")
      val devNull: ActorRef[Any] = ctx.spawn(Behaviors.mutable[Any](_ => new DevNull),"DevNull")

      //sender is not mandatory anymore, if you want to have sender you need to add it to the protocol
      helloActor ! Part1Hello("mainFunction",replyTo = devNull)
//      helloActor ! "Forbidden"
//      helloActor ! 1
      helloActor ! Part1GoodBye
      //stop system after all
      Behaviors.stopped
    }

    val system=ActorSystem(main,"HelloSystem")

    import scala.concurrent.duration._
    Await.result(system.whenTerminated, 3.seconds)


    //show test
  }


  sealed trait Part1Command
  case class Part1Hello(who:String,replyTo:ActorRef[String]) extends Part1Command
  case object Part1GoodBye extends Part1Command

  //Be sure to import scaladsl!!!
  //for both mutable behavior and actorContext
  class MutableTyped() extends MutableBehavior[Part1Command] {

    private var state:Option[String] = None

    private var sender:ActorRef[String] = _

    override def onMessage(msg: Part1Command): Behavior[Part1Command] = msg match {
      case Part1Hello(who,replyTo) =>
        state = Some(who)
        sender=replyTo
        //some thread from actor pool
        println(s"Hello to $who in thread ${Thread.currentThread().getName}")
        this
      case Part1GoodBye =>
        println(s"bye to ${state.getOrElse("UNKNOWN")}")
        sender ! s"bye to ${state.getOrElse("UNKNOWN")}"
        Behaviors.stopped // actor died!!!
    }
  }


  class DevNull extends MutableBehavior[Any]{
    override def onMessage(msg: Any): Behavior[Any] = {
      //some thread from actor pool
      println(s"DEV NULL $msg in thread ${Thread.currentThread().getName}")
      Behaviors.same
    }
  }


}
