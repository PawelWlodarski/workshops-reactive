package jug.workshops.reactive.akka.routing.exercises

import akka.actor.{Actor, ActorRef, Props}
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope

object RoutersPart3ProtocolTransmissionExercise {

  object TransmissionByte {
    def bytesToString(bytes: Seq[TransmissionByte]): String =
      bytes.map(_.toText).mkString
  }

  sealed trait TransmissionByte {
    def toText(): String = this match {
      case One => "1"
      case Zero => "0"

    }
  }

  case object One extends TransmissionByte

  case object Zero extends TransmissionByte

  case class Transmission(id: Int, part: TransmissionByte)

  case class TransmissionWord(bytes: List[TransmissionByte])

  class Transmitter(receiver: ActorRef, bandStart: Int) extends Actor {

    var currentId = bandStart

    override def receive: Receive = {
      case TransmissionWord(bytes) => ???
    }
  }

  class TransmissionReceiver(processor: ActorRef) extends Actor {
    var transmissions = Map[Int, List[TransmissionByte]]()

    override def receive: Receive = ???
  }

  object TransmissionReceiver{
    def props(processor:ActorRef) = Props(new TransmissionReceiver(processor))
  }

}
