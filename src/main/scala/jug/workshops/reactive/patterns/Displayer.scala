package jug.workshops.reactive.patterns

import akka.actor.Actor

class Displayer extends Actor{
  override def receive: Receive = {
    case msg => println(s"received $msg from ${sender.path.toStringWithoutAddress}")
  }
}
