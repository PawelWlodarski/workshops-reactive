package jug.workshops.reactive.patterns.requestreply.exercises

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

/**
  * Created by pawel on 10.10.16.
  */
object RequestReplyDemo {

  def main(args: Array[String]): Unit = {
    val system=ActorSystem("RequestReplyDemo")

    val server=system.actorOf(Props[Server],"server")
    val client=system.actorOf(Props(new Client(server)),"client")

    client ! "START"

    TimeUnit.MILLISECONDS.sleep(500)
    system.terminate()
  }

  import Server._

  class Client(server:ActorRef) extends Actor{
    override def receive: Receive = {
      case "START" =>
        println(s"client starting in ${Thread.currentThread().getName}")
        (1 to 5).foreach{i=>
          server ! Request(s"task$i",self)
        }
      case r : Response[_] =>
        println(s"client received $r in ${Thread.currentThread().getName}")
    }
  }

  class Server extends Actor{


    var thread:ActorRef = _

    //exercise : remove worker name
    @scala.throws[Exception](classOf[Exception])
    override def preStart(): Unit = thread = context.actorOf(Props[ThreadWorker],"worker1")  // why not in constructor

    override def receive: Receive = {
      case msg @ Request(body,from) =>
        println(s"server ${self.path} received $body in ${Thread.currentThread().getName}")
        thread ! msg
    }
  }

  class ThreadWorker extends Actor{
    override def receive: Receive = {
      case Request(body,from) =>
        println(s"worker ${self.path} doing JOB on ${body} in ${Thread.currentThread().getName}") //not 'this'!
        from ! Response(200,s"jobs done! on ${body}")
    }
  }

  object Server{
    case class Request[A](body:A,from:ActorRef)
    case class Response[A](status:Int,body:A)
  }

}
