package jug.workshops.reactive.patterns.adt.exercises

import akka.actor.{Actor, ActorSystem, Props}
import jug.workshops.reactive.patterns.adt.exercises.ActorInterface._

/**
  * ADT MESSAGE PATTERN
  */
//(1) - ADT messages
//(2) - PATTERN MATCHING ON ADT
//(3) - wrong letters utilization
object ADTDemo {

  def main(args: Array[String]): Unit = {
    val system=ActorSystem("ADT")

    val actor=system.actorOf(Props[ActorInterface],"adtInterface")

    actor ! Variant2(1,"test")

    system.terminate()
  }

}

class ActorInterface extends Actor{
  override def receive: Receive = {
    case message :ADTMessage => interface(message)
    case other => throw new RuntimeException(s"unexpected message $other")  // handling death letters
  }


  def interface(message :ADTMessage) = message match {
    case Variant1 => println("variant1")
    case Variant2(id,name) => println(s"variant1 :  $id : $name")  // (2)comment this line - WWARNING
  }

}

//ADT SEALED TYPE (1)
object ActorInterface {
  sealed trait ADTMessage
  final case object Variant1 extends ADTMessage
  final case class Variant2(id:Int, content:String) extends ADTMessage
}
