package jug.workshops.reactive.akka.basics.answers

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.TimeUnit

import akka.actor.{Props, ActorSystem, Actor, ActorRef}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Random


/**
  * Created by pawel on 26.03.16.
  */
object A1ActorState5 {

  def main(args: Array[String]): Unit = {
    val system=ActorSystem("actors-and-state")
    val receiver=system.actorOf(Props[Receiver])
    //DEMONSTRATION
    (1 to 3)
      .map(_ => Props(new Forwarder(receiver)))
      .map(props=>system.actorOf(props))
      .zipWithIndex
        .foreach{ case (forwarder,index) =>
          forwarder ! s"message number $index"
        }

    //EXERCISE
    TimeUnit.SECONDS.sleep(1)
    receiver ! "show_messages"

    system.terminate()
    Await.result(system.whenTerminated, Duration.Inf)
  }

}



class Forwarder(receiver:ActorRef) extends Actor with Clock with RandomTime{
  override def receive: Actor.Receive = {
    case msg =>
      TimeUnit.MILLISECONDS.sleep(randomMillis)
      println(s"FORWARDER : received message '$msg' and now forwarding ${time}...")
      receiver ! s"forwarded $msg"
  }
}

class Receiver extends Actor with Clock{

  var messages:List[Any]=List.empty

  override def receive: Receive = {
    case "show_messages" => messages foreach println
    case msg => println(s"${time} : RECEIVER : received '$msg' "); messages= msg :: messages
  }
}

trait Clock{

  def time : String = {
    val formatter=new SimpleDateFormat("hh:mm:ss.SSS")
    formatter.format(Calendar.getInstance().getTime)
  }
}
trait RandomTime{
  def randomMillis:Int= new Random().nextInt(1000)
}