package jug.workshops.reactive.patterns.requestreply.answers

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import jug.workshops.reactive.patterns.requestreply.answers.DelegateNode.DelegatedTask
import jug.workshops.reactive.patterns.requestreply.answers.SimpleNode._

/**
  * Created by pawel on 09.10.16.
  */
object SubmitTaskAnswer {

  def main(args: Array[String]): Unit = {
    val system=ActorSystem("RequestReply")

    val simpleNode=system.actorOf(Props[SimpleNode],"simpleNode") //EXERCISE 1
    val delegateNode=system.actorOf(Props[DelegateNode],"delegateNode")  //EXERCISE 2
    val client=system.actorOf(Props(new Client(delegateNode)),"client")

    client ! "START"

    TimeUnit.MILLISECONDS.sleep(1000)
    system.terminate()
  }

}

class Client(server: ActorRef) extends Actor{
  override def receive: Receive = {
    case "START" =>
      server ! SquareRoot(16.0)
      server ! Sum(Seq(1,2,3,4,5))
      server ! Sum(1.0 to 1000.0 by 1)
      server ! SquareRoot(25.0)
      server ! SquareRoot(36.0)
      server ! SquareRoot(49.0)
      server ! Sum(1.0 to 10.0 by 1)

    case Response(body) =>
      println(s"client received response from server $body")
  }
}

//EXERCISE 1 - SIMPLE NODE

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


//EXERCISE 2 - DYNAMIC POOL

class DelegateNode extends Actor{

  var childCounter = 0

  override def receive: Receive = {
    case task : Task =>
      val child=context.actorOf(Props[PerRequestWorker],generateChildName)
      child ! DelegatedTask(sender,task)
      println(s"delegated ${task} to ${child.path}")
  }

  def generateChildName:String = {
    val name = s"child$childCounter"
    childCounter=childCounter+1
    name
  }
}

class PerRequestWorker extends Actor {
  override def receive: Receive = {
    case DelegatedTask(replyTo,SquareRoot(number)) =>
      latency
      replyTo ! Response(Math.sqrt(number))
    case DelegatedTask(replyTo,Sum(numbers)) =>
      latency
      replyTo ! Response(numbers.sum)
  }

  def latency={
    println(s"delegate ${self.path.name} working in thread ${Thread.currentThread().getName}")
    TimeUnit.MILLISECONDS.sleep(500)
  }
}

object DelegateNode {
  import SimpleNode._

  case class DelegatedTask(replyTo:ActorRef, task:Task)
}

