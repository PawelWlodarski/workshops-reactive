package jug.workshops.reactive.streams.intro.exercises

import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.actor.{Actor, ActorRef}
import akka.stream.actor.{ActorSubscriber, OneByOneRequestStrategy, RequestStrategy}
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}

import scala.concurrent.Future
import scala.util.{Success, Try}

object StreamsIntroExercise1StreamConstruction {


  object Exercise1{
    //Exercise 1 :
    val numberCandidates: List[String] = List("1", "2", "c", "4", "e", "6", "g", "h", "9")
    //explain why list not iterable
    val source1: Source[String, NotUsed] = ??? //initiate with list

    val flow1: Flow[String, Int, NotUsed] = ???   //filter out non-numbers and convert to int

    val sink1: Sink[Int, Future[Int]] = ???   //use Sink.reduce or Sink.fold

    val exercise1: RunnableGraph[Future[Int]] = source1.via(flow1).toMat(sink1)(Keep.right)
  }

  object Exercise2{
    //Exercise2
    case class Product(id: Int, name: String, price: BigDecimal)

    //explain this type  and how List can help  //http://hseeberger.github.io/blog/2013/10/25/attention-seq-is-not-immutable/
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

    //when stream is completed forward state to receiver
    class SumAllPricesActor(receiver:ActorRef) extends Actor{
      import SumAllPricesActor._

      var state=BigDecimal(0)

      override def receive: Receive = {
        case Exercise2Completed => ???
        case _ => ???
      }

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
//handle possible messages Exercise3Init -> Exercise3Ack , BigInt -> ModifyState , Exercise3Complete -> forward state
      override def receive: Receive = {
        case _ => ???
      }
    }



    def sink3(sink:ActorRef) = Sink.actorRefWithAck(
      sink,Exercise3Init,Exercise3ACK,Exercise3Complete)
  }


}
