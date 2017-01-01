package jug.workshops.reactive.streams.intro.answers

import akka.actor.Actor
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Source, SourceQueue, SourceQueueWithComplete}

import scala.util.{Success, Try}

object StreamsIntro2LogsAnswer {


  val debugSource: Source[String, SourceQueueWithComplete[String]] =
    Source.queue(bufferSize = 2,overflowStrategy = OverflowStrategy.backpressure)
  val errorSource: Source[String, SourceQueueWithComplete[String]] =
    Source.queue(bufferSize = 2,overflowStrategy = OverflowStrategy.backpressure)

  val extractDebug=Flow[String]
      .map(LogsInterpreter.content("\\[DEBUG\\] "))
      .collect{case Success(log) => log}
  val extractError=Flow[String]
    .map(LogsInterpreter.content("\\[ERROR\\] "))
    .collect{case Success(log) => log}



  object LogsInterpreter{
      def content(pattern:String)(s:String) = Try(s.split(pattern)(1))
  }

  class LogsIntoStreamsRouter(debugs:SourceQueue[String], errors:SourceQueue[String]) extends Actor{
    override def receive: Receive = {
      case s:String if (s contains "DEBUG")=> debugs.offer(s)
      case s:String if (s contains "ERROR")=> errors.offer(s)

    }
  }

}
