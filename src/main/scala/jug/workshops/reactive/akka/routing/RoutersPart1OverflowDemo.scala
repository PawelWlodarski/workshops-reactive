package jug.workshops.reactive.akka.routing

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.routing.RoundRobinPool

object RoutersPart1OverflowDemo {

  import scala.concurrent.duration._
  //TURN ON VISUALWM AND CHECK SIMULATION
  def main(args: Array[String]): Unit = {
      val system=ActorSystem("overflow")

      //Manipulate RoundRobinPool size to observe difference
      val destination=system.actorOf(RoundRobinPool(1).props(Props[Destination]))
      val source=system.actorOf(Props(new Source(destination)))

      println("press enter")
      scala.io.StdIn.readLine()

      source ! "START"

  }

  case class Message(content:String)

  object Message{
    val largeString:String=(1 to 100000).map(_.toChar).mkString("")

    def large=Message(largeString+System.currentTimeMillis())
  }


  class Source(destination:ActorRef) extends Actor with ActorLogging{

    implicit val ec=context.dispatcher
    var sentMessages = 0

    override def receive: Receive = {
      case "START" =>
        log.info("SIMULATION STARTED")
        scheduleNext()
      case msg:Message =>
        destination ! msg
        sentMessages=sentMessages+1
        if(sentMessages % 100 == 0 ) log.info(s"SOURCE : sent $sentMessages messages")
        if(sentMessages == 1000) context.system.terminate()
        scheduleNext()
    }
    def scheduleNext(): Unit = {
      context.system.scheduler.scheduleOnce(1 millis) {
        self ! Message.large
      }
    }
  }

  class Destination extends Actor with ActorLogging{

    var processedMessages = 0

    override def receive: Receive = {
      case msg =>
        TimeUnit.MILLISECONDS.sleep(50)
        processedMessages =processedMessages + 1
        if(processedMessages % 100 == 0 ) log.info(s"DESTINATION : processed $processedMessages messages")
    }
  }

}
