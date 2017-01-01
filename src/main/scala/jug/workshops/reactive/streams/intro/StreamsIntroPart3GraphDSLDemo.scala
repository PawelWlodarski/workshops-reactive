package jug.workshops.reactive.streams.intro

import akka.Done
import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, FlowShape}

import scala.concurrent.{Await, Future}

object StreamsIntroPart3GraphDSLDemo {

  def main(args: Array[String]): Unit = {

    implicit val system=ActorSystem("Graph_example")
    implicit val materializer=ActorMaterializer()

    val domainActor=system.actorOf(Props[DomainActor])

    //GRAPH DECLARATION
    import GraphDSL.Implicits._
    val graphWithDomainLogging=GraphDSL.create(){implicit builder =>
      val broadcast=builder.add(Broadcast[String](2))

      val additionalOutput=Sink.actorRef(domainActor,"COMPLETE_MESSAGE")

      broadcast.out(0) ~> additionalOutput

      FlowShape(broadcast.in,broadcast.out(1))
    }

    //FLOW
    val source=Source(List("one","two","three","four","five"))
    val flow=Flow.fromGraph(graphWithDomainLogging)
    val sink= Sink.foreach(println)

    val graph: RunnableGraph[Future[Done]] =source.via(flow).toMat(sink)(Keep.right)

    val futureResult=graph.run()

    import scala.concurrent.duration._
    Await.result(futureResult, 1 second)

    system.terminate()
  }




  class DomainActor extends Actor{
    override def receive: Receive = {
      case message => println(s"Domain actor received $message")
    }
  }

}
