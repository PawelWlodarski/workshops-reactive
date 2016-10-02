package jug.workshops.reactive.patterns.pipesandfilters.exercises

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

/**
  * Created by pawel on 01.10.16.
  */
object PipeStandardDemo {

  def main(args: Array[String]): Unit = {

    val system=ActorSystem("PipeDemo")

    val sink=system.actorOf(Props(classOf[Sink]))
    val trimmer = system.actorOf(Props(classOf[TrimmingPhase],sink))
    val lowerer = system.actorOf(Props(classOf[LoweringPhase],trimmer))
    val entry = system.actorOf(entryActorProps(lowerer))


    entry ! "  AAA  "
    entry ! "BBB  "
    entry ! 123

    TimeUnit.MILLISECONDS.sleep(100)
    system.terminate()

  }

  //Domain
  case class DomainClass(content:String)

  //factories
  def entryActorProps(next:ActorRef) :Props = Props(classOf[EntryActor],next)

  //actors
  // entry ~~>
  class EntryActor(nextPhase:ActorRef) extends Actor{
    override def receive = {
      case raw:String => nextPhase ! DomainClass(raw)
    }
  }

  //~~>phase~~>
  class LoweringPhase(nextPhase : ActorRef) extends Actor{
    override def receive: Receive = {
      case DomainClass(content) =>
        nextPhase ! DomainClass(content.toLowerCase)
    }
  }

  class TrimmingPhase(nextPhase : ActorRef) extends Actor {
    override def receive = {
      case DomainClass(content) =>
        nextPhase ! DomainClass(content.trim)
    }
  }

  //~~> sink
  class Sink extends Actor{

    var datastore:Seq[String] = Vector()

    override def receive: Receive = {
      case DomainClass(content) =>
        println(s"adding '$content'")
        datastore = datastore :+ content
    }
  }

}


