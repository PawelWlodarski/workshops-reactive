package jug.workshops.reactive.streams.intro

import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source, SourceQueueWithComplete}
import akka.stream.{ActorMaterializer, OverflowStrategy}

object StreamsIntroPart2QueueDemo {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("StreamQueue")
    implicit val materializer = ActorMaterializer()

    //PREPARING QUEUE
    val source: Source[String, SourceQueueWithComplete[String]] =
      Source.queue[String](bufferSize = 4, overflowStrategy = OverflowStrategy.backpressure)

    val flow: Flow[String, String, NotUsed] =Flow[String]
      .filter(_ != "")
      .map(_.toUpperCase)
      .map("element from actor to stream: "+_)

    val sink=Sink.foreach(println)

    //EXPLAIN WHY Keep.left /EXPERIMENT, Keep.right , Keep.both
    val graph: RunnableGraph[SourceQueueWithComplete[String]] =source.via(flow).toMat(sink)(Keep.left)

    //SHOW IMPLEMENTATION OF QUEUE
    val queue: SourceQueueWithComplete[String] =graph.run()

    //QUEUE IS GOING TO BE USED BY AN ACTOR
    val actor=system.actorOf(Props(new Deduplicator(queue)))

    actor ! "aaa"
    actor ! "aaa"
    actor ! "aaa"
    actor ! "bbb"
    actor ! "bbb"
    actor ! "ccc"
    actor ! ""
    actor ! ""
    actor ! "ddd"
    actor ! "ddd"

    TimeUnit.SECONDS.sleep(1)
    system.terminate()
  }


  class Deduplicator(q:SourceQueueWithComplete[String]) extends Actor{

    var history=Set[String]()

    override def receive: Receive = {
      case s:String if(!history.contains(s)) =>
        history = history + s
        q.offer(s)
    }
  }
}
