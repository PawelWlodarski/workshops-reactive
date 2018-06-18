package jug.workshops.db.slick.answers

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.testkit.TestKit
import common.StopSystemAfterAll
import jug.workshops.db.slick.DatabaseOperations
import org.scalatest.{FunSuiteLike, MustMatchers}
import slick.basic.DatabasePublisher
import slick.lifted.ProvenShape
import slick.jdbc.H2Profile.api._

import scala.concurrent.{Await, Future}
import scala.util.{Success, Try}

class SlickStreamingAnswers extends TestKit(ActorSystem("SlickStreaming")) with FunSuiteLike
  with MustMatchers with StopSystemAfterAll with DatabaseOperations {

  implicit val materializer = ActorMaterializer()

  test("create source with number candidates"){
    Exercise1.source1
      .runWith(TestSink.probe)
      .request(5)
      .expectNext("1", "2", "c", "4", "e")
      .request(4)
      .expectNext("6", "g", "h", "9")
      .expectComplete()
  }

  test("convert proper numbers to integers"){
    val (pub, sub) = TestSource.probe[String]
      .via(Exercise1.flow1)
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

  test("add all received integers") {
    val resultFuture: Future[Int] = Source(List(1, 2, 3, 4, 5)).runWith(Exercise1.sink1)

    import scala.concurrent.duration._

    val result = Await.result(resultFuture, 1 second)
    result mustBe 15
  }


  test("read numbers from db"){
    val create = Exercise2.numbersTable.schema.create
    val init = Exercise2.numbersTable ++= List("1", "2", "c", "4", "e", "6", "g", "h", "9").map((0,_))

    withInitiatedDb(create,init){db =>

      val selectAll = Exercise2.numbersTable.result
      val publisher: DatabasePublisher[(Int, String)] = db.stream(selectAll)

      val takeOnlyValues = Flow[(Int,String)].map(_._2)

      Source
        .fromPublisher(publisher)
        .via(takeOnlyValues)
        .via(Exercise1.flow1)
        .runWith(TestSink.probe)
        .request(6)
        .expectNext(1,2,4,6,9)
        .expectComplete()


    }
  }


  object Exercise1{
    //Exercise 1 :
    val numberCandidates: List[String] = List("1", "2", "c", "4", "e", "6", "g", "h", "9")
    //explain why list not iterable
    val source1: Source[String, NotUsed] = Source(numberCandidates)

    val flow1: Flow[String, Int, NotUsed] = Flow[String]
      .map(s => Try(s.toInt))
      .collect { case Success(i) => i }

    val sink1: Sink[Int, Future[Int]] = Sink.reduce[Int](_ + _)

    val exercise1: RunnableGraph[Future[Int]] = source1.via(flow1).toMat(sink1)(Keep.right)
  }




  object Exercise2{
    class Numbers(tag: Tag) extends Table[(Int,String)](tag, "NUMBER") {
      def id:Rep[Int] = column[Int]("ID",O.PrimaryKey,O.AutoInc)
      def number: Rep[String] = column[String]("NUMBER")
      def * : ProvenShape[(Int,String)] = (id,number)
    }

    val numbersTable = TableQuery[Numbers]
  }


}
