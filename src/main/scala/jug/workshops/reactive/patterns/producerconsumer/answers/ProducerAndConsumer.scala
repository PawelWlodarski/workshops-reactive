package jug.workshops.reactive.patterns.producerconsumer.answers

import akka.actor.{Actor, ActorRef}

import scala.concurrent.duration.FiniteDuration

object ProducerAndConsumer {

  object StartProducer
  final case class Element(e:Int)
  case object GetConsumedState
  case class ConsumedState(s:Vector[Int])

}

import ProducerAndConsumer._

class SimpleProducer(consumer:ActorRef, elements:Iterable[Int]) extends Actor{
  override def receive: Receive = {
    case StartProducer =>
      elements.map(Element.apply).foreach(consumer ! _)
  }
}

class SimpleConsumer extends Actor{
  var consumed=Vector.empty[Int]


  override def receive: Receive = {
    case Element(e) =>  consumed = consumed :+ e
    case GetConsumedState => sender() ! ConsumedState(consumed)
  }
}

class ProducerPush(elements:Iterable[Int],interval:FiniteDuration) extends Actor{
  override def receive: Receive = ???
}
