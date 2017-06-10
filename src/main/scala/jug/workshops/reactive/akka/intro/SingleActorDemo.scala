package jug.workshops.reactive.akka.intro

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.Logging
import jug.WorkshopDisplayer

object SingleActorDemo extends WorkshopDisplayer{


  def main(args: Array[String]): Unit = {
    //STANDARD CLASS EXAMPLE
    appendToStandardClass()

    //AKKA EXAMPLE
    //Actorsystem is a factory for an actor
    val system=ActorSystem("intro")

    unhandledExample(system)
    system.terminate()
  }

  //how actor serialize access to state - spawn multiple threads
  //untyped -> location transparency
  //why messages has to be immutable
  //receive - partial function[Any,Unit] why?
  //dead letter actors

  private def appendToStandardClass() = {
    val instance = new StandardClass
    (1 to 30).par.foreach { e =>
      instance.append(e)
    }

    TimeUnit.MILLISECONDS.sleep(100)

    section("instance state")
    instance.display
  }

  private def appendToActor(system:ActorSystem) = {
    //factory argument - props factory in companion object
    val props=Props[SomeActor]

    //factory invocation - why ActorRef and not object instance?
    val actor:ActorRef=system.actorOf(props)

  }
  def unhandledExample(system:ActorSystem): Unit = {
      val deafActor=system.actorOf(DeafActor.props)
      deafActor ! "aaaa"
      deafActor ! 69
      TimeUnit.SECONDS.sleep(1)
  }

}

import SomeActor._
class SomeActor extends Actor {

  var state=List.empty[Int]

  override def receive: Receive = {
    case Append(i) => state = state :+ i
    case Display => println("Actor : " + state.mkString(","))
  }
}

object SomeActor{
  case class Append(e:Int)
  case object Display

  //props factory
  // * problem of capturing this and instances
}

class StandardClass {
  private var state=List.empty[Int]

  def append(elem:Int) = state = state :+ elem

  def display = println("Standard class : " + state.mkString(","))
}

class DeafActor extends Actor{
  val log=Logging(context.system,"identifier")  //why context? logging is blocking
  override def receive: Receive = PartialFunction.empty[Any,Unit]

  override def unhandled(message: Any): Unit = message match{
    case msg:String => log.info(s"unhandled $msg")
    case msg => super.unhandled(msg)
  }
}

object DeafActor{
  def props= Props[DeafActor]  //no closure here, benefits of object factory?
}
