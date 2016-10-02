package jug.workshops.reactive.patterns.pipesandfilters.answers

import akka.actor.ActorSystem
import akka.testkit.TestKit
import common.StopSystemAfterAll
import org.scalatest.{MustMatchers, WordSpecLike}

/**
  * Created by pawel on 01.10.16.
  */
class PipeStandardAnswersTest extends TestKit(ActorSystem("testsystem"))
  with WordSpecLike with MustMatchers with StopSystemAfterAll {

  import jug.workshops.reactive.patterns.pipesandfilters.answers.PipeStandardAnswer._

  "Pipes and filters" should {

    "accept raw order in the endpoint" in {
      val endpoint=system.actorOf(endpointProps(testActor))
      val rawOrderBytes: Array[Byte] =  TransportLib.pack("TEST")
      endpoint ! rawOrderBytes

      expectOrderWithContent("TEST")
    }

    "decrypt message" in {
      val decrypter=system.actorOf(decrypterProps(testActor))
      val message=ProcessorIncomingOrder(TransportLib.pack("(encryption)TEST{rest}"))

      decrypter ! message

      expectOrderWithContent("TEST{rest}")
    }

    "authenticate message" in {
      val authenticator=system.actorOf(authenticatorProps(testActor))
      val message=ProcessorIncomingOrder(TransportLib.pack("(certificate)(encryption)TEST"))

      authenticator ! message

      expectOrderWithContent("(encryption)TEST")
    }

    "remove duplicates" in {
      import scala.concurrent.duration._
      val deduplicator=system.actorOf(deduplicatorProps(testActor))
      val message=ProcessorIncomingOrder(TransportLib.pack("<order id='123'>Tv</order>"))


      deduplicator ! message
      deduplicator ! message
      deduplicator ! message

      expectOrderWithContent("<order id='123'>Tv</order>")
      expectNoMsg(100 millis)
    }
  }

  "parsing lib" should {
    "parse id" in {
      val order = "<order id='123'>...</order>"
      val result = OrderParser.parseId(order)

      result mustBe Some("123")
    }
  }

  def expectOrderWithContent(expected:String): Unit = {
    expectMsgPF() {
      case ProcessorIncomingOrder(content) =>
        TransportLib.unpack(content) mustBe expected

      case msg => fail(s"unknown message $msg")
    }
  }
}
