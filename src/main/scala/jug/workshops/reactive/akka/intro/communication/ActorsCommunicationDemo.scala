package jug.workshops.reactive.akka.intro.communication

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import PingPongProtocol._
import akka.event.LoggingAdapter

object ActorsCommunicationDemo {

  def main(args: Array[String]): Unit = {
    val system=ActorSystem()

    println(s"START")
    println(s"MAIN thread : ${Thread.currentThread()}")

    val pongActor = system.actorOf(Props[PongActor],"PongActor")
    val pingActor = system.actorOf(Props(new PingActor(pongActor)),"PingActor")

    pingActor ! StartGame(12)

    TimeUnit.SECONDS.sleep(1)
    system.terminate()
  }
}

object PingPongProtocol{
  case class StartGame(c:Int)
  case object Ping
  case object Pong
  def displaythread(log:LoggingAdapter)() = log.info(Thread.currentThread().getName)
}

//Dependency Injection
class PingActor(pongActor:ActorRef) extends Actor with ActorLogging{
  var gameLength:Int = 0

  override def receive: Receive = {
    case StartGame(c) =>
      displaythread(log)
      log.info(s"ping actor initialized with $c")
      gameLength = c
      pongActor ! Pong


    case Ping if gameLength >0 =>
      gameLength=gameLength - 1
      displaythread(log)
      log.info(s"Pong : $gameLength")
      pongActor ! Pong
    case Ping =>
      log.info("GAMe OVER")
  }
}

class PongActor extends Actor with ActorLogging{
  var state:Int=0
  override def receive: Receive = {
    case Pong =>
      displaythread(log)
      log.info(s"Ping")
      sender() ! Ping   //EXPLAIN SENDER!!
  }
}