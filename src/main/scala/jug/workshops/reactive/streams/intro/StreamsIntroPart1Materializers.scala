package jug.workshops.reactive.streams.intro

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Status}
import akka.stream.{ActorMaterializer, OverflowStrategy, ThrottleMode}
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}

import scala.concurrent.{Await, Future}


object StreamsIntroPart1Materializers {

  def main(args: Array[String]): Unit = {

    import scala.concurrent.duration._

    implicit val system = ActorSystem("StreamQueue")
    implicit val materializer = ActorMaterializer()

    val source: Source[Int, ActorRef] =Source.actorRef(bufferSize = 4,overflowStrategy = OverflowStrategy.dropBuffer)
    val flow=Flow[Int].map(_+1)
    val sink: Sink[Int, Future[Int]] = Sink.fold(0)(_+_)

    //DIFFERENT MATERIALIZEr OPTIONS
    val p1: Source[Int, ActorRef] =source.via(flow)
    val p2: Source[Int, NotUsed] =source.viaMat(flow)(Keep.none)
    val p3: Source[Int, NotUsed] = source.viaMat(flow)(Keep.right)

    val p4: Sink[Int, NotUsed] =flow.to(sink)
    val p5: Sink[Int, Future[Int]] = flow.toMat(sink)(Keep.right)

    val p6: RunnableGraph[ActorRef] =source.via(flow).to(sink)
    val p7: RunnableGraph[Future[Int]] =source.via(flow).toMat(sink)(Keep.right)
    val p8: RunnableGraph[(ActorRef, Future[Int])] =source.via(flow).toMat(sink)(Keep.both)

    //SHORTCUT FOR MATERIALIZING ALL VALUES
//    val p9: (ActorRef, Future[Int]) =flow.runWith(source,sink)

    val (actorRef,future)=p8.run()

    actorRef ! 1
    actorRef ! 2
    actorRef ! 3
    actorRef ! 4
    actorRef ! Status.Success("CONTENT IGNORED")  //SIGNALIZE END OF COMMUNICATION

    val result=Await.result(future,1 second)
    println(s"fold result $result")

    system.terminate()

  }

}
