package jug.workshops.reactive.streams.intro.exercises

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import common.StopSystemAfterAll
import jug.workshops.reactive.streams.intro.exercises.StreamsIntroExercise1StreamConstruction.Exercise2.SumAllPricesActor.Exercise2Completed
import jug.workshops.reactive.streams.intro.exercises.StreamsIntroExercise1StreamConstruction.Exercise3.SumAllPricesActorWithAck.{Exercise3ACK, Exercise3Complete, Exercise3Init}
import org.scalatest.{MustMatchers, WordSpecLike}

import scala.concurrent.{Await, Future}

class StreamsIntroExercise1StreamConstructionSpec extends TestKit(ActorSystem("streams")) with WordSpecLike
  with MustMatchers with StopSystemAfterAll with ImplicitSender{

  import StreamsIntroExercise1StreamConstruction.Exercise1._

  implicit val materializer = ActorMaterializer()

  "Exercise 1 Stream" should {

//http://doc.akka.io/docs/akka/2.4/scala/stream/stream-testkit.html
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

  import StreamsIntroExercise1StreamConstruction.Exercise2._
//http://doc.akka.io/docs/akka/2.4/scala/stream/stream-testkit.html
  "Exercise 2 Stream" should {
    "create source with future result" in {
      source2
        .runWith(TestSink.probe)
        .request(???)  //expect 3 elements
        .expectNext(
          ???,???,???   //you should receive all products from source2
        )
        .expectComplete()
    }

    //uncomment and make it compile and pass
//    "extract prices from product" in {
//    //use flow2 from StreamsIntroExercise1StreamConstruction here
//    // flow.runWith(source,sink)  <--- use here test sink and test source with proper types
//      val (pub: akka.stream.testkit.TestPublisher.Probe[Product],
//          sub: akka.stream.testkit.TestSubscriber.Probe[BigDecimal]) = ???
//
//      sub.request(n = ???)
//      pub.sendNext(???)
//      pub.sendNext(???)
//
//      sub.expectNext(BigDecimal(20),BigDecimal(120))
//    }
//you need to finish implementation in StreamsIntroExercise1StreamConstruction - don't change test
    "sum all prices" in {
      val probe=TestProbe()

      val sink=system.actorOf(Props(new SumAllPricesActor(probe.ref)))

      source2.via(flow2).to(sink2(sink,Exercise2Completed)).run()

      probe.expectMsg(BigDecimal(140))

    }
  }

  import StreamsIntroExercise1StreamConstruction.Exercise3._

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
