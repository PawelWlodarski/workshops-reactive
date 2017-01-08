package jug.workshops.reactive.streams.intro.answers

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Broadcast, GraphDSL, Sink, Source, Zip}
import akka.stream.{ActorMaterializer, SinkShape, SourceShape}
import akka.testkit.TestKit
import common.StopSystemAfterAll
import org.scalatest.{FlatSpecLike, MustMatchers}

import scala.concurrent.{Await, Future}

class StreamsIntroExercise3GraphDSLSpecAnswer extends TestKit(ActorSystem("streams")) with FlatSpecLike
  with MustMatchers with StopSystemAfterAll {

  import GraphDSL.Implicits._

  import scala.concurrent.duration._

  implicit val materializer = ActorMaterializer()

  // EXERCISE1
  "Source created from graph" should "save messages in external collection" in {
    //STORE ALL ELEMENTS IN THIS COLLECTION
    var history = List[String]()
    val elementsToEmit = List("one", "two", "three")

    //GRAPH
    val sourceWithHistory = GraphDSL.create() { implicit builder =>

      val broadcast = builder.add(Broadcast[String](2))

      val historySink: Sink[String, Future[Done]] = Sink.foreach { (e: String) =>
        history = e :: history
      }

      val source = Source(elementsToEmit)

      source ~> broadcast.in
      broadcast.out(0) ~> historySink

      SourceShape(broadcast.out(1))
    }

    //when
    val resultFuture: Future[Done] = Source.fromGraph(sourceWithHistory).runWith(Sink.ignore)

    Await.result(resultFuture, 1 second) //why we need to wait here?

    //then
    history must contain inOrder("three", "two", "one")
  }


  //EXERCISE2 - Explain imported sink
  "sink create from graph" should "zip inputs" in {
    var history = List[(String,Int)]()
    val elementsToEmit = List("one", "two", "three")
    val historySink: Sink[(String,Int), Future[Done]] = Sink.foreach { e =>history = e :: history}

    //GRAPH - imported additional sink to keep materialized value
    val zippingSink = GraphDSL.create(historySink) { implicit  builder => importedSink =>
      val fromZero: Iterator[Int] = Iterator.iterate(0)(_ + 1)
      val source = Source.fromIterator(() => fromZero)
      val zipper = builder.add(Zip[String, Int])

      source ~> zipper.in1
      zipper.out ~> importedSink

      SinkShape(zipper.in0)
    }

    //when
    val resultFuture = Source(elementsToEmit).runWith(Sink.fromGraph(zippingSink))

    Await.result(resultFuture,1 second)

    //then
    history must contain inOrder(("three",2), ("two",1), ("one",0))
  }


}
