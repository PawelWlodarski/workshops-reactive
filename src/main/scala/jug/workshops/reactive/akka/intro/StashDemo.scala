package jug.workshops.reactive.akka.intro
import java.util.concurrent.TimeUnit

import Switch._
import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Stash}

object StashDemo {

  def main(args: Array[String]): Unit = {
    val system=ActorSystem("actor_state")
    import Switch._

    val switch = system.actorOf(Props[SwitchWithStash])

    //Is there a situation where messages will be reordered?
    switch ! Message("msg1")
    sleep(200)
    switch ! Message("msg2")
    sleep(400)
    switch ! Message("msg3")
    sleep(700)
    switch ! Message("msg4")

    sleep(200)
    switch ! Message("msg5")
    switch ! "Off"
    switch ! Message("msg6")
    sleep(500)
    system.terminate()
  }

}


import scala.concurrent.duration.FiniteDuration

class SwitchWithStash extends Actor with ActorLogging with Stash{
  override def receive: Receive = waitingForInitialization

  import scala.concurrent.ExecutionContext.Implicits.global

  context.system.scheduler.scheduleOnce(FiniteDuration(1,"s"),self,Initialized)

  val waitingForInitialization : Receive = {
    case Initialized =>
      context.become(initialized)
      log.info("unstashing")
      unstashAll()
    case msg =>
      stash()
      log.warning(s"received message while uninitialized $msg")
  }

  val initialized : Receive = {
    case Message(content) => log.info(s"receive message while initialized $content")
    case "Off" => context.unbecome()
  }

}

object SwitchWithStash {
  private[intro] case object Initialized
  case class Message(content:String)

  def sleep(n:Int) = TimeUnit.MILLISECONDS.sleep(n)
}