package jug.workshops.reactive.streams.intro.answers

import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.actor.{Actor, ActorRef}
import akka.stream.actor.{ActorSubscriber, OneByOneRequestStrategy, RequestStrategy}
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}

import scala.concurrent.Future
import scala.util.{Success, Try}

object StreamsIntroExercise1StreamConstructionAnswer {


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
    //Exercise2
    case class Product(id: Int, name: String, price: BigDecimal)

    //explain this type  and how List can help
    type Iterable[A] = scala.collection.immutable.Iterable[A]

    private object Database {


      private val data = Map[Int, Product](
        1 -> Product(1, "Techno Greatest Hits 1995-97", BigDecimal(20)),
        2 -> Product(2, "Bazooka", BigDecimal(120)),
        3 -> Product(3, "MAc Book USB adapter", BigDecimal(1000))
      )

      import scala.concurrent.ExecutionContext.Implicits.global
      def selectAll(): Future[Iterable[Product]] =
        Future {
          TimeUnit.MILLISECONDS.sleep(500)
          data.values.to[Iterable]  //explain
        }
    }

    //source2 is completed - finish test in StreamsIntroExercise1StreamConstructionSpec
    val source2: Source[Product, NotUsed] = Source
      .fromFuture(Database.selectAll())
      .mapConcat(identity)  //explain why this is working, can use List


    //remove everything more expensive than 300 coins and convert to price
    //flow2 is implemented, complete test in StreamsIntroExercise1StreamConstructionSpec
    val flow2: Flow[Product, BigDecimal, NotUsed] = Flow[Product]
      .filter(_.price < BigDecimal(300))
      .map { case Product(id, name, price) => price }

    object SumAllPricesActor{
      case object Exercise2Completed
    }

    class SumAllPricesActor(receiver:ActorRef) extends ActorSubscriber{
      import SumAllPricesActor._

      var state=BigDecimal(0)

      override def receive: Receive = {
        case price:BigDecimal => state = state + price
        case Exercise2Completed => receiver ! state
      }

      override protected def requestStrategy: RequestStrategy = OneByOneRequestStrategy
    }

    def sink2(sink:ActorRef,onComplete:Any) = Sink.actorRef(sink,onComplete)
  }

  object Exercise3{

    object SumAllPricesActorWithAck{
      case object Exercise3Init
      case object Exercise3ACK
      case object Exercise3Complete
    }

    import SumAllPricesActorWithAck._

    class SumAllPricesActorWithAck(receiver:ActorRef) extends Actor{

      var state=BigDecimal(0)

      override def receive: Receive = {
        case Exercise3Init => sender ! Exercise3ACK
        case price:BigDecimal =>
          state = state + price
          sender ! Exercise3ACK

        case Exercise3Complete =>
          receiver ! state


      }
    }



    def sink3(sink:ActorRef) = Sink.actorRefWithAck(
      sink,Exercise3Init,Exercise3ACK,Exercise3Complete)
  }


}
