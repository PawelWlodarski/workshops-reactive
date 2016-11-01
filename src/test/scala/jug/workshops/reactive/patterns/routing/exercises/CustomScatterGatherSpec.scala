package jug.workshops.reactive.patterns.routing.exercises

import akka.actor.{ActorSystem, Props}
import akka.routing.Broadcast
import akka.testkit.{ImplicitSender, TestKit}
import common.StopSystemAfterAll
import jug.workshops.reactive.patterns.routing.exercises.CustomScatterGatherExercise._
import org.scalatest.{MustMatchers, WordSpecLike}

class CustomScatterGatherSpec extends TestKit(ActorSystem()) with MustMatchers
    with WordSpecLike with StopSystemAfterAll with ImplicitSender{

  import scala.concurrent.duration._


  "Worker" should {
    "detect word" in {
      val worker=system.actorOf(Props(new DictionaryWorker(100)))

      worker ! Detect("komputer")

      expectMsg(200 millis,Detected(Some("polish")))
    }
  }

  "Custom Router" should {
    "consider only the fastest message" in {
      val props=Seq(100,500,2000).map(t=>Props(new DictionaryWorker(t)))

      val router=system.actorOf(Props(new CustomSGRouter(props)))

      router ! Detect("computer")

      expectMsg(200 millis,Detected(Some("english")))
      expectNoMsg()

    }

    "brodcast message to workers" in {
      val props=Seq(100,100,2000).map(t=>Props(new DictionaryWorker(t)))

      val router=system.actorOf(Props(new CustomSGRouter(props)))

      router ! Broadcast(New("english","street"))

      router ! Detect("street")
      router ! Detect("street")
      router ! Detect("street")

      expectMsg(150 millis,Detected(Some("english")))
      expectMsg(150 millis,Detected(Some("english")))
      expectMsg(250 millis,Detected(Some("english")))

      expectNoMsg()
    }
  }


  "Dictionary" should {
    "detect word when there is language" in {
      val detected = Dictionary.detect(dictionary)("komputer")

      detected mustBe Some("polish")
    }

    "return empty when there is no language" in {
      val detected = Dictionary.detect(dictionary)("buenos!!")

      detected mustBe None
    }

    "update language when there is existing entry" in {
      val updated=Dictionary.update(dictionary)("polish","raz")

      updated must contain only(
        "polish" -> List("raz","komputer", "przeglądarka", "klawiatura", "mysz", "programowanie"),
        "english" -> List("computer", "browser", "keyboard", "mouse", "programming"),
        "esperanto" -> List("komputilo", "retumilo", "klavaro", "muso", "programado")
       )
    }


    "update language when there is no existing entry" in {
      val updated=Dictionary.update(dictionary)("zulu","ikhompyutha")

      updated must contain only(
        "polish" -> List("komputer", "przeglądarka", "klawiatura", "mysz", "programowanie"),
        "english" -> List("computer", "browser", "keyboard", "mouse", "programming"),
        "esperanto" -> List("komputilo", "retumilo", "klavaro", "muso", "programado"),
        "zulu" -> List("ikhompyutha")
        )
    }
  }

  var dictionary: Map[String, List[String]] = Map(
    "polish" -> List("komputer", "przeglądarka", "klawiatura", "mysz", "programowanie"),
    "english" -> List("computer", "browser", "keyboard", "mouse", "programming"),
    "esperanto" -> List("komputilo", "retumilo", "klavaro", "muso", "programado")
  )
}
