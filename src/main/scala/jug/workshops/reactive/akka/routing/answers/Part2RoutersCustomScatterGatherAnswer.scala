package jug.workshops.reactive.akka.routing.answers

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.routing.Broadcast
import akka.util.Timeout

import scala.collection.immutable.IndexedSeq
import scala.concurrent.Future
import scala.util.Random

object Part2RoutersCustomScatterGatherAnswer {


  case class Detect(word: String)

  case class Detected(language: Option[String])

  case class New(language: String, word: String)

  /**
    *
    * @param workers - props of routed workers
    * @param timeout - milliseconds
    */
  class CustomSGRouter(workers: Seq[Props], timeout:Long=1000) extends Actor {

    private implicit val internalTimeout=Timeout(timeout,TimeUnit.MILLISECONDS)
    private implicit val ec=context.dispatcher

    val pool: Seq[ActorRef] ={
      workers.map(prop=>context.actorOf(prop))
    }

    override def receive: Receive = {
      case Broadcast(message) =>
        pool.foreach(_ ! message)
      case message =>
          val originalSender=sender
          val futures=pool.map(_ ? message)
          val first=Future.firstCompletedOf(futures)
          first.onSuccess{
            case response => originalSender ! response
          }
    }
  }

  class DictionaryWorker(sleepTime:Int=RandomSleep.sleep()) extends Actor {

    import Dictionary._

    var dictionary: Map[String, Set[String]] = Map(
      "polish" -> Set("komputer", "przeglądarka", "klawiatura", "mysz", "programowanie"),
      "english" -> Set("computer", "browser", "keyboard", "mouse", "programming"),
      "esperanto" -> Set("komputilo", "retumilo", "klavaro", "muso", "programado")
    )

    override def receive: Receive = {
      case Detect(word) =>
        TimeUnit.MILLISECONDS.sleep(sleepTime)
        val detected = detect(dictionary)(word)
        sender ! Detected(detected)

      case New(language,word) =>
        dictionary=update(dictionary)(language,word)
    }
  }

  object Dictionary{
    type Dictionary=Map[String,Set[String]]

    def detect(dictionary:Dictionary)(word:String): Option[String] =dictionary.find {
      case (lang, words) => words.contains(word)
    }.map(_._1)

    def update(dictionary: Dictionary)(language:String,word:String):Dictionary={
      val words = dictionary.getOrElse(language, Set.empty[String])
      val newWords=words + word
      dictionary + (language->newWords)
    }
  }

  object RandomSleep {

    private val periods: IndexedSeq[Int] = IndexedSeq(2000,100, 300,  Int.MaxValue, 5000)

    def sleep(): Int = {
      val sleepMillis = periods(new Random().nextInt(5))
      TimeUnit.MILLISECONDS.sleep(sleepMillis)
      sleepMillis
    }
  }

}
