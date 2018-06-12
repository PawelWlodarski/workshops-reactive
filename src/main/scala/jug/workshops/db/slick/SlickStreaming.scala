package jug.workshops.db.slick

import akka.NotUsed
import akka.actor.ActorSystem

object SlickStreaming extends DatabaseOperations{

  //slick imports
  import slick.jdbc.H2Profile.api._

  //stream imports
  import akka.stream._
  import akka.stream.scaladsl._

  def main(args: Array[String]): Unit = {
    val source=Source(1 to 100)

    val flow=Flow[Int].map(_+1)

    val sink=Sink.foreach{ input:Int =>
      println(s"procesing $input in thread ${Thread.currentThread().getName}")
    }


    val stream: RunnableGraph[NotUsed] =source.via(flow).to(sink)


    ///this part can be decoupled easily
    val system=ActorSystem("slickstream1")
    val materializer=ActorMaterializer()(system)

    stream.run()(materializer)



  }
}
