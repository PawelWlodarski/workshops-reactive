package jug.workshops.reactive.akka.basics.exercises

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

/**
  * Created by pawel on 27.03.16.
  */
object A1PingPong {
  import PingPongProtocol._
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

  //EXERCISE 1 - IMPLEMENT CASE CLASSES 'BALL' and 'BALL2(INT)'
}


class Player1 extends Actor{
  var ballCounter=0

  override def receive: Receive = {
    ???
    //EXERCISE - handle case when ball is sent, to send it back use 'sender()' method
    //EXERCISE - handle case when ball_with_counter is sent, to send it back use 'sender()' method
  }
}

class Player2(p1: ActorRef) extends Actor {

  override def receive:Receive={
    case msg =>
      println("PING")
      handler(msg)
  }

  def handler: Actor.Receive = {
    ???
    //EXERCISE - Handle case when 'Play()' is send - you need to start the game with normal Ball
    //EXERCISE - Handle case when 'PlayBallCounter(n)' is send - you need to start the game with Ball with counter
    //EXERCISE - handle case when Ball() is send back by player 1
    //EXERCISE - handle case when BallWithCounter(n) is send back by player 1
  }
}
