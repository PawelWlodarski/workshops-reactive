package jug.workshops.reactive.patterns.requestreply.exercises

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import jug.workshops.reactive.patterns.requestreply.exercises.DelegateNode._
import jug.workshops.reactive.patterns.requestreply.exercises.SimpleNode._

import scala.collection.immutable.Queue

/**
  * Created by pawel on 09.10.16.
  */
object SubmitTaskAnswer {

  def main(args: Array[String]): Unit = {
    val system=ActorSystem("RequestReply")

    val simpleNode=system.actorOf(Props[SimpleNode],"simpleNode") //EXERCISE 1
    val delegateNode=system.actorOf(Props[DelegateNode],"delegateNode")  //EXERCISE 2
    val poolNode=system.actorOf(Props(new PoolNode(poolSize = 3)),"poolNode")  //EXERCISE 3
    val client=system.actorOf(Props(new Client(simpleNode)),"client")

    client ! "START"

    TimeUnit.MILLISECONDS.sleep(30000)
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
    ???
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
      child ! DelegatedTask(???,???)  // replace ??? with proper values in delegated task
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
      ??? // handle message properly
    case DelegatedTask(replyTo,Sum(numbers)) =>
      latency
      ??? // handle message , send response to proper actor
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


//EXERCISE3
class PoolNode(poolSize:Int) extends Actor {

  import PoolNode._
  import context._

  var pool:Queue[ActorRef] = Queue()
  val tasks:scala.collection.mutable.Queue[DelegatedTask] = scala.collection.mutable.Queue()

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    (1 to poolSize).foreach{i =>
      val worker = context.actorOf(Props[PoolWorker],s"pool-worker$i")
      pool = pool enqueue worker
    }
  }

  override def receive: Receive = {
    case task : Task =>
      //1) pool is immutable , dequeue returns (element,newPool)
      //2) Send delegated taks to worker
      ???
      if(pool.isEmpty) become(poolEmpty)
  }

  def poolEmpty:Receive={
    //handle 3 cases
    // 1)Worker finished computation and there are new tasks on queue
    // 2) worker finished computation and there are no new tasks
    // 3) new task is send

    ???
  }
}

class PoolWorker extends Actor{
  import PoolNode._

  override def receive: Receive = {
    //In all cases you need to send reponse to client and notify server that worker is ready for new task
    case DelegatedTask(replyTo,SquareRoot(number)) =>
      latency
      ???
    case DelegatedTask(replyTo,Sum(numbers)) =>
      latency
      ???
    case msg => throw new RuntimeException(s"unhandled message $msg")
  }

  def latency={
    println(s"worker ${self.path.name} working in thread ${Thread.currentThread().getName}")
    TimeUnit.MILLISECONDS.sleep(500)
  }
}

object PoolNode {

  case class ComputationFinished(worker:ActorRef)

}
