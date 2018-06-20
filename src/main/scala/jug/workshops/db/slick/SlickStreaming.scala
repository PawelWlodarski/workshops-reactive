package jug.workshops.db.slick

import java.util.concurrent.TimeUnit

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.event.Logging
import slick.basic.DatabasePublisher
import slick.lifted.ProvenShape

import scala.concurrent.Future

object SlickStreaming extends DatabaseOperations{

  //slick imports
  import slick.jdbc.H2Profile.api._

  //stream imports
  import akka.stream._
  import akka.stream.scaladsl._

  def main(args: Array[String]): Unit = {
    simpleStreamExample()
//    materializedValue()
//    streamingFromSlick()
//    asyncAndThreads()

  }


  class CoffeesWithId(tag: Tag) extends Table[(Int,String, Double)](tag, "COFFEES") {
    def id:Rep[Int] = column[Int]("ID",O.PrimaryKey,O.AutoInc)
    def name: Rep[String] = column[String]("COF_NAME")
    def price: Rep[Double] = column[Double]("PRICE")
    def * : ProvenShape[(Int,String, Double)] = (id,name, price)
  }

  lazy val coffees = TableQuery[CoffeesWithId]

  private def simpleStreamExample() = {
    val source = Source(1 to 100)

    val flow = Flow[Int].map(_ + 1)

    val sink = Sink.foreach { input: Int =>
      println(s"procesing $input in thread ${Thread.currentThread().getName}")
    }


    val stream: RunnableGraph[NotUsed] = source.via(flow).to(sink)


    ///this part can be decoupled easily
    val system = ActorSystem("slickstream1")
    val materializer = ActorMaterializer()(system)

    stream.run()(materializer)

    system.terminate()
  }

  private def materializedValue() = {
    val source = Source(1 to 10)

    val flow = Flow[Int].map(_ + 1)

    val sink: Sink[Int, Future[String]] = Sink.fold("START")(_+":"+_)


    val stream: RunnableGraph[NotUsed] = source.via(flow).to(sink)
    val streamWithValue: RunnableGraph[Future[String]] = source.via(flow).toMat(sink)(Keep.right)


    ///this part can be decoupled easily
    val system = ActorSystem("slickstream1")
    val materializer = ActorMaterializer()(system)

    val res=waitForResult(streamWithValue.run()(materializer))

    println(s"strim with value result $res")

    system.terminate()
  }



  private def streamingFromSlick() = {
    withInitiatedDb(coffees.schema.create) { db =>

      //INITIALIZATION
      val insertAction = coffees ++= Seq(
        (0, "caffe1", 1.5),
        (0, "caffe2", 2.5),
        (0, "caffe3", 3.5),
        (0, "caffe4", 4.5)
      )

      waitForResult(db.run(insertAction))

      val takeAll = coffees.result.withStatementParameters(
        fetchSize = 3
      )

      val s: DatabasePublisher[(Int, String, Double)] = db.stream(takeAll)

      val source: Source[(Int, String, Double), NotUsed] = Source.fromPublisher[(Int, String, Double)](s)

      val flow = Flow[(Int, String, Double)].map { case (id, name, price) =>
        println(s"in FLOW thread : ${Thread.currentThread().getName}")
        (id, name.toUpperCase, price)
      }


      val sink = Sink.foreach[(Int, String, Double)] { case (id, name, price) =>
        println(s" ($id, $name, $price)")
      }


      implicit val system = ActorSystem("slickstream2")
      implicit val materializer = ActorMaterializer()

      val stream: RunnableGraph[Future[Done]] = source.via(flow).toMat(sink)(Keep.right)


      val r: Future[Done] = stream.run()
      waitForResult(r)
      //      implicit val ec=system.dispatcher
      //      s.foreach(println)
      //      TimeUnit.SECONDS.sleep(1)

      waitForResult(system.terminate())

    }
  }

  private def asyncAndThreads() = {
    withInitiatedDb(coffees.schema.create) { db =>

      //INITIALIZATION
      val insertAction = coffees ++= Seq(
        (0, "caffe1", 1.5),
        (0, "caffe2", 2.5),
        (0, "caffe3", 3.5),
        (0, "caffe4", 4.5)
      )

      waitForResult(db.run(insertAction))

      val takeAll = coffees.result.withStatementParameters(
        fetchSize = 3
      )

      val s: DatabasePublisher[(Int, String, Double)] = db.stream(takeAll)

      val source: Source[(Int, String, Double), NotUsed] =
        Source.fromPublisher[(Int, String, Double)](s)
        .named("mySource")
//        .log("SourceLog")
//          .withAttributes(
//            Attributes.logLevels(
//              onElement = Logging.WarningLevel,
//              onFinish = Logging.InfoLevel,
//              onFailure = Logging.DebugLevel
//            )
//          )
        .map{r=>
          println(s"procesing source $r in thread ${Thread.currentThread().getName}")
          r
        }

      import scala.concurrent.duration._
      val flow = Flow[(Int, String, Double)]
//        .async   // <----- check async!
        .map { case (id, name, price) =>
        println(s"procesing flow $name in thread ${Thread.currentThread().getName}")
        (id, name.toUpperCase, price)
      }
      .throttle(2, 1 second)


      val sink = Sink
      .foreach[(Int, String, Double)] { case (id, name, price) =>
        println(s" ($id, $name, $price) in sink : ${Thread.currentThread().getName}")
      }
//        .async //check async


      implicit val system = ActorSystem("slickstream2")
      implicit val materializer = ActorMaterializer()

      val stream: RunnableGraph[Future[Done]] =
        source
          .via(flow)
          .toMat(sink)(Keep.right)


      waitForResult(stream.run(),howManySeconds = 3)
      waitForResult(system.terminate())

    }
  }






}
