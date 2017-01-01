package jug.workshops.reactive.streams.intro.exercises

import akka.NotUsed
import akka.actor.Actor
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.SourceQueue
import akka.stream.scaladsl.{Flow, Source, SourceQueueWithComplete}

import scala.util.{Success, Try}

object StreamsIntro2LogsExercise {


  val debugSource: Source[String, SourceQueueWithComplete[String]] = ???
  val errorSource: Source[String, SourceQueueWithComplete[String]] = ???

  val extractDebug: Flow[String, String, NotUsed] = ???
  val extractError: Flow[String, String, NotUsed] = ???

  //What is the advantage of currying here?
  object LogsInterpreter{
      def content(pattern:String)(s:String) = ???
  }

  class LogsIntoStreamsRouter(debugs:SourceQueue[String], errors:SourceQueue[String]) extends Actor{
    override def receive: Receive = {
      case s:String if (s contains "DEBUG")=> debugs.offer(s)
      case s:String if (s contains "ERROR")=> errors.offer(s)

    }
  }

}
