package jug.workshops.reactive.streams.intro.exercises

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.TestSink
import akka.testkit.{ImplicitSender, TestKit}
import common.StopSystemAfterAll
import org.scalatest.{MustMatchers, WordSpecLike}

import scala.util.{Success, Try}


class StreamsIntro2LogsExerciseSpec extends TestKit(ActorSystem("streams")) with WordSpecLike
  with MustMatchers with StopSystemAfterAll with ImplicitSender {

  implicit val materializer = ActorMaterializer()


  import StreamsIntro2LogsExercise._

  "Logs exercise " should {
    "properly initiate two flows" in {
      val (debugQueue, debugProbe) = debugSource.via(extractDebug).toMat(TestSink.probe)(Keep.both).run()
      val (errorQueue, errorProbe) = errorSource.via(extractError).toMat(TestSink.probe)(Keep.both).run()

      val router = system.actorOf(Props(new LogsIntoStreamsRouter(debugQueue, errorQueue)))


      router ! "2014-07-02 20:52:39 [DEBUG] className:200 - This is debug message"
      router ! "2014-07-02 20:52:39 [DEBUG] className:201 - This is debug message2"
      router ! "2014-07-02 20:52:39 [ERROR] className:203 - This is error message"
      router ! "2014-07-02 20:52:39 [ERROR] className:204 - This is error message2"
      router ! "2014-07-02 20:52:39 [DEBUG] className:205 - This is debug message3"

      debugProbe.request(3)
        .expectNext("className:200 - This is debug message")
        .expectNext("className:201 - This is debug message2")
        .expectNext("className:205 - This is debug message3")

      errorProbe.request(3)
        .expectNext("className:203 - This is error message")
        .expectNext("className:204 - This is error message2")

    }
  }

  "Logs interpreter" should {
    "return content of a log line" in {
      val line = "2014-07-02 20:52:39 [DEBUG] className:200 - This is debug message"
      val interpreter: (String) => Try[String] = LogsInterpreter.content("\\[DEBUG\\] ") _

      val result = interpreter(line)

      result mustBe Success("className:200 - This is debug message")
    }
  }

}
