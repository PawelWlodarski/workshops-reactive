package jug.workshops.reactive.streams.intro

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}
import akka.{Done, NotUsed}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future

object StreamsIntroPart1Basics {
  def main(args: Array[String]): Unit = {

    println("*** Akka Streams Intro ***")
    val source: Source[Int, NotUsed] =Source(1 to 15 by 3)
    val flow: Flow[Int, String, NotUsed] =Flow[Int].map(_*2).map(i=>s"as string : $i")
    val sink: Sink[Any, Future[Done]] =Sink.foreach(println)

    val stream: RunnableGraph[NotUsed] =source.via(flow).to(sink)

    println("   * Nothing happened yet...executing")

    //but where are actors?
    val demoConfig = ConfigFactory.load("streams/streamsdemo")
    implicit val system=ActorSystem("streams",demoConfig)
    implicit val materializer=ActorMaterializer()

    stream.run()(materializer)  //make materializer implicit

    println("   * this will be displayed before,during,or after first run (lack of determinism)")
    TimeUnit.MILLISECONDS.sleep(100)
    println("   * reusing elements")

    val streamWithResult: RunnableGraph[Future[Done]] =source.via(flow).toMat(sink)(Keep.right)

    val futureResult: Future[Done] =streamWithResult.run()

    implicit val executionContext = system.dispatchers.lookup("my-dispatcher")

    futureResult.onSuccess{
      case Done => println("this will be displayed after second run")
    }

    println("   *  but where are actors?")
    class ActorSink extends Actor{
      override def receive: Receive = {
        case msg => println(s"Real actor received $msg")
      }
    }

    val actorSink=system.actorOf(Props[ActorSink])
    val realActorSubscriber: Sink[Any, NotUsed] = Sink.actorRef(actorSink,"MY_COMPLETED")

    source.via(flow).to(realActorSubscriber).run()

  }
}
