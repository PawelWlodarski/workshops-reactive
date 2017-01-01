package jug.workshops.reactive.streams.intro.exercises

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, SourceQueueWithComplete}
import akka.stream.testkit.TestSubscriber.Probe
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.testkit.{ImplicitSender, TestKit}
import common.StopSystemAfterAll
import org.scalatest.{MustMatchers, WordSpecLike}

import scala.concurrent.{Await, Future}

class StreamsIntroExercise2DatabaseFlowExerciseSpec extends TestKit(ActorSystem("streams")) with WordSpecLike
  with MustMatchers with StopSystemAfterAll with ImplicitSender {

  import StreamsIntroExercise2DatabaseFlowExercise.DatabaseUserStreams._
  import StreamsIntroExercise2DatabaseFlowExercise.StreamExerciseDomain._
  import StreamsIntroExercise2DatabaseFlowExercise._

  import scala.concurrent.duration._

  implicit val materializer = ActorMaterializer()

  "Exercise 2 stream" should {
    "properly initiate source" in {
      val (queue: SourceQueueWithComplete[Id], probe: Probe[Id]) = source.toMat(TestSink.probe)(Keep.both).run()

      queue.offer(Id(1))
      queue.offer(Id(2))
      queue.offer(Id(5))
      queue.complete()

      probe
        .request(3)
        .expectNext(Id(1), Id(2), Id(5))
        .expectComplete()
    }

    "properly extract email in flow" in {
      val (source,sink)=flow.runWith(TestSource.probe[Id],TestSink.probe[Email])

      source.sendNext(Id(1))
      source.sendNext(Id(2))

      sink
        .request(2)
        .expectNext(Email("george@wp.pl"))
        .expectNext(Email("monique@wp.pl"))

    }

    "send email via sink" in {
      import akka.stream.testkit.TestPublisher.{Probe => PubProbe}
      val (probe:PubProbe[Email],resultFuture: Future[Done])=TestSource.probe[Email].toMat(sink)(Keep.both).run

      probe.sendNext(Email("test@google.com"))
      probe.sendNext(Email("test2@google.com"))
      probe.sendComplete()

      Await.result(resultFuture,1 second)

      EmailService.history must contain allOf(
        Email("test@google.com"),
        Email("test2@google.com")
      )
    }
  }

}
