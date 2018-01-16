package jug.workshops.poligon.reactivedevelopment

import akka.actor.{Actor, ActorLogging}

object Greetings {

  class Greeter extends Actor with ActorLogging{
    override def receive: Receive = {
      case Hello(t) =>
        log.info(s"received hello $t")
        sender() ! Hello("hello you to")
      case  Goodbye =>
        log.info("received goodbye")
        sender() ! Goodbye
      case unknown =>
        log.warning("unknown message {msg}",unknown)
    }
  }

  case class Hello(text:String)
  case object Goodbye
}
