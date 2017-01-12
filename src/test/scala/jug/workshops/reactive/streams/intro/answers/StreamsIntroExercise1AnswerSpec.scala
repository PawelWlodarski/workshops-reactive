package jug.workshops.reactive.streams.intro.answers

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import common.StopSystemAfterAll
import org.scalatest.{MustMatchers, WordSpecLike}

import jug.workshops.reactive.streams.intro.exercises.StreamsIntroExercise1StreamConstruction

import scala.concurrent.{Await, Future}

/**
  * Created by pawel on 06.11.16.
  */
class StreamsIntroExercise1AnswerSpec extends TestKit(ActorSystem("streams")) with WordSpecLike
  with MustMatchers with StopSystemAfterAll with ImplicitSender{

  import StreamsIntroExercise1StreamConstructionAnswer.Exercise1._
  import StreamsIntroExercise1StreamConstructionAnswer.Exercise2._
  import StreamsIntroExercise1StreamConstructionAnswer.Exercise2.SumAllPricesActor._
  import StreamsIntroExercise1StreamConstructionAnswer.Exercise3._
  import StreamsIntroExercise1StreamConstructionAnswer.Exercise3.SumAllPricesActorWithAck._

  implicit val materializer = ActorMaterializer()

  "Exercise 1 Stream" should {

    "create source with number candidates" in {
      source1
        .runWith(TestSink.probe)
        .request(5)
        .expectNext("1", "2", "c", "4", "e")
        .request(4)
        .expectNext("6", "g", "h", "9")
        .expectComplete()
    }


    "convert proper numbers to integers" in {
      val (pub, sub) = TestSource.probe[String]
        .via(flow1)
        .toMat(TestSink.probe[Int])(Keep.both)
        .run()

      sub.request(n = 5)
      pub.sendNext("a")
      pub.sendNext("2")
      pub.sendNext("3")
      pub.sendNext("d")
      pub.sendNext("5")

      sub.expectNext(2, 3, 5)
    }

    "add all received integers" in {
      val resultFuture: Future[Int] = Source(List(1, 2, 3, 4, 5)).runWith(sink1)

      import scala.concurrent.duration._

      val result = Await.result(resultFuture, 1 second)
      result mustBe 15
    }

  }

  "Exercise 2 Stream" should {
    "create source with future result" in {
      source2
        .runWith(TestSink.probe)
        .request(3)
        .expectNext(
          Product(1, "Techno Greatest Hits 1995-97", BigDecimal(20)),
          Product(2, "Bazooka", BigDecimal(120)),
          Product(3, "MAc Book USB adapter", BigDecimal(1000))
        )
        .expectComplete()
    }

    "extract prices from product" in {
      val (pub, sub) = TestSource.probe[Product]
        .via(flow2)
        .toMat(TestSink.probe[BigDecimal])(Keep.both)
        .run()

      sub.request(n = 2)
      pub.sendNext(Product(1, "Techno Greatest Hits 1995-97", BigDecimal(20)))
      pub.sendNext(Product(2, "Bazooka", BigDecimal(120)))

      sub.expectNext(BigDecimal(20),BigDecimal(120))
    }

    "sum all prices" in {
      val probe=TestProbe()

      val sink=system.actorOf(Props(new SumAllPricesActor(probe.ref)))

      source2.via(flow2).to(sink2(sink,Exercise2Completed)).run()

      probe.expectMsg(BigDecimal(140))

    }
  }

  "Exercise 3 Stream" should {

    "properly implement protocol" in {
      val probe=TestProbe()

      val sink=system.actorOf(Props(new SumAllPricesActorWithAck(probe.ref)))

      sink ! Exercise3Init
      expectMsg(Exercise3ACK)

      sink ! BigDecimal(10)
      expectMsg(Exercise3ACK)

      sink ! BigDecimal(20)
      expectMsg(Exercise3ACK)

      sink ! Exercise3Complete
      probe.expectMsg(BigDecimal(30))
    }

    "sum all prices" in {
      val probe=TestProbe()

      val sink=system.actorOf(Props(new SumAllPricesActorWithAck(probe.ref)))

      source2.via(flow2).to(sink3(sink)).run()

      probe.expectMsg(BigDecimal(140))

    }
  }
}

