package jug.workshops.reactive.patterns.routing

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.routing.ScatterGatherFirstCompletedPool

import scala.concurrent.duration._
import scala.util.Random

/**
  * Each worker sleeps random time period and then response. Only first response is resend.
  */
object RoutersScatterGatherDemo {

  def main(args: Array[String]): Unit = {

    val system = ActorSystem("ScatterGather")

    val sender=system.actorOf(Props[Sender],"sender")

    sender ! "START"

    TimeUnit.SECONDS.sleep(5)
    system.terminate()
  }

  case class MessageId(val v: Int) extends AnyVal

  case class Request(id: MessageId)

  case class Response(requestId: MessageId, from: String)

  class Sender extends Actor with ActorLogging{

    val worker=context.actorOf(
      ScatterGatherFirstCompletedPool(3,within = 6 seconds )
        .props(Props[Worker]),"workerPool"
    )
    override def receive: Receive = {
      case "START" =>
         worker ! Request(MessageId(1))
         worker ! Request(MessageId(2))

      case Response(id,description) =>
        log.info(s"response $id received from $description")
    }
  }

  class Worker extends Actor with ActorLogging {
    override def receive: Receive = {
      case Request(id) =>
        val describeSelf = s"${self.path.toStringWithoutAddress}"
        val slept = RandomSleep.sleep()
        log.info(s"$describeSelf calculated $id for $slept")
        sender ! Response(id,describeSelf )
    }
  }

  object RandomSleep {

    private val periods: IndexedSeq[Int] = IndexedSeq(100, 500, 1000, 2000, 5000)

    def sleep(): Int = {
      val sleepMillis = periods(new Random().nextInt(5))
      TimeUnit.MILLISECONDS.sleep(sleepMillis)
      sleepMillis
    }
  }

}

