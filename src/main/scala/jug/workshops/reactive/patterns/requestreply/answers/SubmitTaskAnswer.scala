package jug.workshops.reactive.patterns.requestreply.answers

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import jug.workshops.reactive.patterns.requestreply.answers.SimpleNode._

/**
  * Created by pawel on 09.10.16.
  */
object SubmitTaskAnswer {

  def main(args: Array[String]): Unit = {
    val system=ActorSystem("RequestReply")

    val simpleNode=system.actorOf(Props[SimpleNode],"simpleNode")
    val client=system.actorOf(Props(new Client(simpleNode)),"client")

    client ! "START"

    TimeUnit.MILLISECONDS.sleep(500)
    system.terminate()
  }

}

class Client(server: ActorRef) extends Actor{
  override def receive: Receive = {
    case "START" =>
      server ! SquareRoot(16.0)
      server ! Sum(Seq(1,2,3,4,5))

    case Response(body) =>
      println(s"client received response from server $body")
  }
}


class SimpleNode extends Actor{
  import SimpleNode._

  override def receive: Receive = {
    case SquareRoot(number) => sender ! Response(Math.sqrt(number))
    case Sum(numbers) => sender ! Response(numbers.sum)
  }
}

object SimpleNode{
  sealed trait Task
  case class SquareRoot(number:Double) extends Task
  case class Sum(numbers:Seq[Double]) extends Task

  case class Response(body:Double)
}
