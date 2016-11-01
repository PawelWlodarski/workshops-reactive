package jug.workshops.reactive.patterns.routing.exercises

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.routing.Broadcast
import akka.util.Timeout

import scala.collection.immutable.IndexedSeq
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Random

object CustomScatterGatherExercise {

  //Protocol messages
  //request to detected new word
  case class Detect(word: String)
  //response with detected language
  case class Detected(language: Option[String])
  //request to add new word to given language
  case class New(language: String, word: String)

  /**
    *
    * @param workers - props of routed workers
    * @param timeout - milliseconds
    */
  class CustomSGRouter(workers: Seq[Props], timeout:Long=1000) extends Actor {

    private implicit val internalTimeout: Timeout = ??? //use timeout from constructor to set internal timeout for futures
    private implicit val ec: ExecutionContextExecutor = ??? //global,custom,dispatcher - what are the consequences?

    val pool: Seq[ActorRef] = ??? //initiate workers

    override def receive: Receive = {
      case Broadcast(message) => ??? //send message to each worker

      case message => ??? //send message to each worker but handle only first response
      // (ask pattern, futures, Future.firstCompletedOf)

    }
  }

  class DictionaryWorker(sleepTime:Int=RandomSleep.sleep()) extends Actor {

    import Dictionary._

    var dictionary: Map[String, List[String]] = Map(
      "polish" -> List("komputer", "przeglądarka", "klawiatura", "mysz", "programowanie"),
      "english" -> List("computer", "browser", "keyboard", "mouse", "programming"),
      "esperanto" -> List("komputilo", "retumilo", "klavaro", "muso", "programado")
    )

    override def receive: Receive = {
      case Detect(word) =>
        TimeUnit.MILLISECONDS.sleep(sleepTime)
        ??? // detect language and send back response

      case New(language,word) =>
        ??? // update dictionary
    }
  }

  //you can use this component or write logic inside actors, what are consequences?
  object Dictionary{
    type Dictionary=Map[String,List[String]]

    def detect(dictionary:Dictionary)(word:String): Option[String] = ???

    def update(dictionary: Dictionary)(language:String,word:String):Dictionary= ???
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
