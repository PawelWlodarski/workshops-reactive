package jug.workshops.reactive.akka.basics.exercises

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

import scala.concurrent.Await
import scala.concurrent.duration.Duration


/**
  * Created by pawel on 26.03.16.
  */
object BasicsPart5State {

  def main(args: Array[String]): Unit = {
    val system=ActorSystem("actors-and-state")

    val history=system.actorOf(Props[History])
    val forwarder=system.actorOf(Props(new Forwarder(history)))

    forwarder ! "MSG1"
    forwarder ! "MSG2"
    TimeUnit.MILLISECONDS.sleep(300)
    forwarder ! "MSG3"
    forwarder ! "MSG4"

//    TimeUnit.MILLISECONDS.sleep(300)  // explain messages order
    history ! "DISPLAY"

    system.terminate()
    Await.result(system.whenTerminated, Duration(2,TimeUnit.SECONDS))
  }

}


class Forwarder(actorRef:ActorRef) extends Actor{
  override def receive: Receive = {
    case message:String => actorRef ! s"[message:$message, time:${Clock.time}]"
    case unhandled => throw new RuntimeException(s"unhandled message $unhandled")
  }
}

class History extends Actor with ActorLogging{

  var history=Vector[String]()

  override def receive: Receive = {
    case "DISPLAY" =>  log.info(history.mkString("\n","\n","\n"))
    case msg:String => history = history :+ msg
  }
}

object Clock{
  def time : String = {
    val formatter=new SimpleDateFormat("hh:mm:ss.SSS")
    formatter.format(Calendar.getInstance().getTime)
  }
}
