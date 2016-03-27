package jug.workshops.reactive.akka.a1.exercises

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
    // 1 - store messages in actor
    // 2 - display all stored messages when "show_messages" is sent
    // 3 - add time information to when message was sent and when it was received
    // 4 - wait random time in forwarders before rsending message
//    comment/uncomment this line :TimeUnit.SECONDS.sleep(1)
//    receiver ! "show_messages"
    system.terminate()
    Await.result(system.whenTerminated, Duration(2,TimeUnit.SECONDS))
  }

}



class Forwarder(receiver:ActorRef) extends Actor{
  override def receive: Actor.Receive = {
    case msg =>
      //EXERCISE 4- wait random time
      println(s"FORWARDER : received message '$msg' and now forwarding {time}...")
      receiver ! s"forwarded $msg"
  }
}

class Receiver extends Actor{
  //EXERCISE 3 - add time to messag
  var messages:List[Any]=List.empty

  override def receive: Receive = {
    //EXERCISE 2 - show all stored messages
    case msg => println(s"RECEIVER : received '$msg' ") //EXERCISE 1 STORE MESSAGE  //EXERCISE 3 - add time info
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