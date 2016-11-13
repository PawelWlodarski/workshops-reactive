package jug.workshops.reactive.akka.routing.answers

import akka.actor.{Actor, ActorRef, Props}
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope

object RoutersPart3ProtocolTransmissionAnswer {

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
      case TransmissionWord(bytes) =>
        bytes
          .map(b => Transmission(currentId, b))
          .map(t => ConsistentHashableEnvelope(t, currentId))
          .foreach(receiver ! _)

        currentId=currentId+1
    }
  }

  class TransmissionReceiver(processor: ActorRef) extends Actor {
    var transmissions = Map[Int, List[TransmissionByte]]()

    override def receive: Receive = {
      case Transmission(id, part) if (transmissions.contains(id)) =>
        updateTransmission(id, part)
        handleFullMessage(id)
      case Transmission(id, part) =>
        transmissions=transmissions + (id -> List(part))
    }

    def updateTransmission(id: Int, part: TransmissionByte): Unit = {
      transmissions.get(id).map(part :: _).foreach { updated =>
        transmissions = transmissions + (id -> updated)
      }
    }

    def handleFullMessage(id: Int): Unit = {
      transmissions.get(id).filter(_.length == 4).foreach { transmission =>
        transmissions = transmissions - id
        processor ! TransmissionByte.bytesToString(transmission.reverse)
      }
    }
  }

  object TransmissionReceiver{
    def props(processor:ActorRef) = Props(new TransmissionReceiver(processor))
  }

}
