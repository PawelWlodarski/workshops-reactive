package jug.workshops.reactive.akka.a1.answers

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

/**
  * Created by pawel on 27.03.16.
  */
import jug.workshops.reactive.akka.a1.answers.PingPongProtocol._
object A1PingPong {

  def main(args: Array[String]) {
    val system=ActorSystem("PingPong")

    val p1=system.actorOf(Props[Player1])
    val p2=system.actorOf(Props(new Player2(p1)))

    p2 ! Play()
//    p2 ! PlayBallCounter(30)

    TimeUnit.SECONDS.sleep(2)
    system.terminate()
  }
}

object PingPongProtocol{
  case class Play()
  case class PlayBallCounter(v:Int)
  case class Ball()
  case class BallWithCounter(v:Int)
}


class Player1 extends Actor{
  var ballCounter=0

  override def receive: Receive = {
    case b @ Ball() if ballCounter < 100 => println(s"PONG : ball counter : $ballCounter"); ballCounter=ballCounter+1;sender() ! b
    case BallWithCounter(v) if v<100  => println(s"PONG : ball has v=$v");sender() ! BallWithCounter(v+1)
  }
}

class Player2(p1: ActorRef) extends Actor {

  override def receive:Receive={
    case msg =>
      println("PING")
      handler(msg)
  }

  def handler: Actor.Receive = {
    case Play() => p1 ! Ball()
    case PlayBallCounter(n) =>p1 ! BallWithCounter(n)
    case b @ Ball() => p1 ! b
    case BallWithCounter(v) => sender() ! BallWithCounter(v+1)
  }
}
