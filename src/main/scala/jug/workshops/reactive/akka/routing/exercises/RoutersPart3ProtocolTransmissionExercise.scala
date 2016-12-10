package jug.workshops.reactive.akka.routing.exercises

import akka.actor.{Actor, ActorRef, Props}
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope

object RoutersPart3ProtocolTransmissionExercise {

  object TransmissionBit {
    def bitToString(bit: Seq[TransmissionBit]): String =
      bit.map(_.toText).mkString
  }

  sealed trait TransmissionBit {
    def toText(): String = this match {
      case One => "1"
      case Zero => "0"

    }
  }

  case object One extends TransmissionBit

  case object Zero extends TransmissionBit

  case class Transmission(id: Int, part: TransmissionBit)

  case class TransmissionWord(bits: List[TransmissionBit])

  class Transmitter(receiver: ActorRef, bandStart: Int) extends Actor {

    var currentId = bandStart

    override def receive: Receive = {
      case TransmissionWord(bits) => ???
    }
  }

  class TransmissionReceiver(processor: ActorRef) extends Actor {
    var transmissions = Map[Int, List[TransmissionBit]]()

    override def receive: Receive = ???
  }

  object TransmissionReceiver{
    def props(processor:ActorRef) = Props(new TransmissionReceiver(processor))
  }

}
