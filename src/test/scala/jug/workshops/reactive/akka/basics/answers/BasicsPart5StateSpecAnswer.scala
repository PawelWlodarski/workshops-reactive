package jug.workshops.reactive.akka.basics.answers

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import common.StopSystemAfterAll
import org.scalatest.{MustMatchers, WordSpecLike}

/**
  * Created by pawel on 30.10.16.
  */
class BasicsPart5StateSpecAnswer extends TestKit(ActorSystem("test")) with MustMatchers
    with WordSpecLike with StopSystemAfterAll with ImplicitSender {

  import scala.concurrent.duration._

  "Deduplicator" should {
    "remove duplicates" in {
        val probe=TestProbe()

        val deduplicator = system.actorOf(Props(new Deduplicator(probe.ref)))
        deduplicator ! "MSG1"
        deduplicator ! "MSG1"
        deduplicator ! "MSG2"
        deduplicator ! "MSG2"
        deduplicator ! "MSG2"
        deduplicator ! "MSG3"

        probe.expectMsg("MSG1")
        probe.expectMsg("MSG2")
        probe.expectMsg("MSG3")
        probe.expectNoMsg(500 millis)
    }
  }

  "Deduplicator -> WordProcessor combination" should {
    "translate deduplicated messages" in {
      val probe=TestProbe()

      val wordProcessor=system.actorOf(Props(new WordProcessor(probe.ref)))
      val deduplicator = system.actorOf(Props(new Deduplicator(wordProcessor)))

      deduplicator ! "message1"
      deduplicator ! "message1"
      deduplicator ! "message2"
      deduplicator ! "message2"
      deduplicator ! "message2"
      deduplicator ! "Message3"

      probe.expectMsg("MESSAGE1")
      probe.expectMsg("MESSAGE2")
      probe.expectMsg("MESSAGE3")
      probe.expectNoMsg(500 millis)

    }
  }


  class Deduplicator(next:ActorRef) extends Actor{

    var history=Set[String]()

    override def receive: Receive = {
      case msg:String if(!history.contains(msg)) =>
        history=history + msg
        next ! msg
    }
  }

  class WordProcessor(next:ActorRef) extends Actor{
    override def receive: Receive = {
      case word:String => next ! word.toUpperCase
    }
  }

}
