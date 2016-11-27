package jug.workshops.reactive.streams.intro

import java.util.concurrent.TimeUnit

import akka.{Done, NotUsed}
import akka.actor.{Actor, ActorSystem, Props}
import akka.routing.RoundRobinPool
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.Timeout

import scala.concurrent.{Await, ExecutionContext, Future}

object StreamsIntroPart2IntegrationDemo {

  //EXPLAIN IMPORTS
  import akka.pattern.ask
  import scala.concurrent.duration._

  def main(args: Array[String]): Unit = {
    implicit val system=ActorSystem("StreamsIntegration")
    //EXPLAIN MATERIALIZER SETTINGS - EXPERIMENT WITH BUFFER
    implicit val materializer=ActorMaterializer(
      ActorMaterializerSettings(system).withInputBuffer(initialSize = 4,maxSize = 4)
    )
    //WHY WE NEED EC FOR FUTURES
    implicit val executionContext=system.dispatcher

    //EXPERIMENT WITH TIMEOUT
    implicit val askTimeout = Timeout(2 second)

    val actor=system.actorOf(Props[ToStringBlocking])   // SINGLE ACTOR
//    val actor=system.actorOf(RoundRobinPool(4).props(Props[ToStringBlocking])) //ROUTER FOR UNORDERED

    val elements=List(1,5,3,2,8,1,1,2,5,8,3)
    val source: Source[Int, NotUsed] = Source(elements)

    val flow: Flow[Int, String, NotUsed] =Flow[Int]
      //SHOW ORDERED/UNORDERED -> SINGLE/ROUTER/FUTURE
      .mapAsync(parallelism = 5)(elem => (actor ? elem).mapTo[String])
//      .mapAsyncUnordered(parallelism = 5)(elem => (actor ? elem).mapTo[String])
//        .mapAsyncUnordered(parallelism = 5)(ToStringService.intToString)
      .map(e=>s"<element>$e</element>")

    println("    ***      STREAM CREATION     ***")
    val sink: Sink[String, Future[Done]] =Sink.foreach[String](println)

    val stream: RunnableGraph[Future[Done]] =source.via(flow).toMat(sink)(Keep.right)


    println("    ***      STREAM EXECUTION     ***")
    val resultFuture: Future[Done] =stream.run()

    Await.result(resultFuture,10 seconds)
    system.terminate()
  }


  class ToStringBlocking extends Actor{
    override def receive: Receive = {
      case input:Int =>
        TimeUnit.MILLISECONDS.sleep(input*100)
        sender ! input.toString
    }
  }

  object ToStringService {
    def intToString(i:Int)(implicit ec:ExecutionContext):Future[String] = Future{
      TimeUnit.MILLISECONDS.sleep(i*100)
      i.toString
    }
  }
}
