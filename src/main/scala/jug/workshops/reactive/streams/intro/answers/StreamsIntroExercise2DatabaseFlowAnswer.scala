package jug.workshops.reactive.streams.intro.answers

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorSystem, Props, Status}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source, SourceQueueWithComplete}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.{Done, NotUsed}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

object StreamsIntroExercise2DatabaseFlowAnswer {
  object StreamExerciseDomain{
    case class Email(value:String) extends AnyVal
  }
  import DatabaseUserStreams._
  import StreamExerciseDomain._

  val source: Source[Id, SourceQueueWithComplete[Id]] =
    Source.queue(bufferSize = 4, overflowStrategy = OverflowStrategy.backpressure)

  val flow: Flow[Id, Email, NotUsed] =Flow[Id]
    .mapAsync(parallelism = 4)(DatabaseUserStreams.read)
    .collect{case Some(record) => record.email}

   val sink: Sink[Email, Future[Done]] =Sink.foreach[Email](EmailService.sendEmail)


  object DatabaseUserStreams{
    case class Id(id:Int) extends AnyVal
    case class StreamsRecord(id:Id, name:String,email:Email,cash:Int)

    private val data=Map[Id,StreamsRecord](
      Id(1) -> StreamsRecord(Id(1),"George",Email("george@wp.pl"),120),
      Id(2) -> StreamsRecord(Id(2),"Monique",Email("monique@wp.pl"),200),
      Id(3) -> StreamsRecord(Id(3),"Zdzislawa",Email("zdzislawa@wp.pl"),1500),
      Id(4) -> StreamsRecord(Id(4),"Stefan",Email("stefan@wp.pl"),0),
      Id(5) -> StreamsRecord(Id(5),"Bonifacy",Email("bonifacy@wp.pl"),2150)
    )

    def read(id:Id):Future[Option[StreamsRecord]]=Future{
      TimeUnit.MILLISECONDS.sleep(50)
       data.get(id)
    }
  }

  object EmailService{
    private var emails=List[Email]()

    def sendEmail(e:Email):Unit ={
      TimeUnit.MILLISECONDS.sleep(100)
      emails = e :: emails
    }


    def history:List[Email] = emails
  }

  object CustomerNotification{
    case object Exercise2Completed
  }

  class CustomerNotification(q:SourceQueueWithComplete[Id]) extends Actor{
    import CustomerNotification._

    override def receive: Receive = {
      case s:String if s.nonEmpty =>
        Try(s.toInt).map(i=>Id(i)) match {  //in scala 2.12 Try.fold is available
          case Success(id) => onSuccess(id)
          case Failure(e) => onError(e)
        }

      case Exercise2Completed => q.complete()
    }

    def onError: Throwable => Unit = e=>sender ! Status.Failure(e)
    def onSuccess: Id => Unit = id => q.offer(id)
  }

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("StreamQueue")
    implicit val materializer = ActorMaterializer()

    val (queue,futureResult)=source.via(flow).toMat(sink)(Keep.both).run()

    val notification=system.actorOf(Props(new CustomerNotification(queue)),"customerNotification")

    notification ! "1"
    notification ! ""
    notification ! "3"
    notification ! "5"

    TimeUnit.MILLISECONDS.sleep(100)
    queue.complete()

    futureResult.onComplete{
      _ => EmailService.history.foreach(println)
    }

    Await.result(futureResult,2 second)

    system.terminate()
  }
}
