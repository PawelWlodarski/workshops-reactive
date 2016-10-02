package jug.workshops.reactive.patterns.pipesandfilters.answers

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.actor.ActorSubscriberMessage.{OnComplete, OnNext}
import akka.stream.actor.{ActorSubscriber, RequestStrategy, WatermarkRequestStrategy}
import akka.stream.scaladsl.{Flow, RunnableGraph, Sink, Source}
import akka.{Done, NotUsed}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by pawel on 02.10.16.
  */
object PipesStreamsAnswer1 {

  def main(args: Array[String]): Unit = {
    import TransportLib._

    //DOMAIN
    object TransportLib {
      //type instead of custom case class - functions already preserve type safety
      type RawMessage = Array[Byte]

      val pack: String => RawMessage = content => content.toCharArray.map(_.toByte)
      val unpack: RawMessage => String = bytes => new String(bytes)

      //high order function - this is actualy "lift" to "domain flow"
      val repack: (String => String) => (RawMessage => RawMessage) = mapper => unpack andThen mapper andThen pack
    }

    object OrderParser {
      def parseId(order: String): Option[String] = {
        val regex = "id='([0-9]+).*'".r

        val matched = regex findFirstMatchIn order
        matched.map(_.group(1))
      }
    }

    //"ACTORS" as testable functions
    object PhasesLib {
      //simple function - easy to test
      val decrypter: (RawMessage) => RawMessage = repack(_.replace("(encryption)", ""))

      val authenticator: (RawMessage) => RawMessage = repack(_.replace("(certificate)", ""))

      //()=>A=>Seq[A] - stateful function factory with safe closure (like in javascript ;))
      val deduplicator = { () =>
        var processedOrderIds = Set[String]()

        val deduplicate = (order: String) =>
          OrderParser.parseId(order) match {
            case Some(id) if (!processedOrderIds(id)) =>
              processedOrderIds = processedOrderIds + id
//              Inspector.displayThread  -- show in debug
              List(order)
            case _ => List()
          }

        val packList: List[String] => List[RawMessage] = _.map(pack)

        unpack andThen deduplicate andThen packList
      }


      class OrderManagement extends Actor  with ActorSubscriber{
        def receive= {
          case OnNext(message:RawMessage)=>
            println(s"ACTOR ORDER MANAGEMENT RECEIVED ORDER ${unpack(message)}")
          case OnComplete  =>
            println(s"ACTOR ORDER MANAGEMENT COMPLETED STREAM")
        }
        protected def requestStrategy: RequestStrategy = WatermarkRequestStrategy(69)
      }
    }

    //FLOW
    import PhasesLib._
    implicit val system = ActorSystem("PipesStream")
    implicit val materializer = ActorMaterializer()

    val orderText: String = "(encryption)(certificate)<order id='123'>${orderData}</order>"

    // IN
    val orderSource: Source[RawMessage, NotUsed] = Source(List(orderText, orderText, orderText).map(pack))

    //THROUGH
    val rawMessageFlow = Flow[RawMessage]
      .map(decrypter)
      .map(authenticator)
      .statefulMapConcat(deduplicator)

    //OUT
    val result: Future[Done] = orderSource
      .via(rawMessageFlow)
      .runForeach(e => println(s"received ${unpack(e)}"))


    Await.result(result, Duration.Inf)
//    runWithActorSubscriber(orderSource, rawMessageFlow)   // Actor as Sink
    system.terminate()



    def runWithActorSubscriber(orderSource: Source[RawMessage, NotUsed], rawMessageFlow: Flow[RawMessage, RawMessage, NotUsed]): Unit = {
      val graph: RunnableGraph[NotUsed] = orderSource.via(rawMessageFlow).to(Sink.actorSubscriber(Props(new OrderManagement)))
      graph.run()
      TimeUnit.SECONDS.sleep(1)
    }
  }


  object Inspector{
    def displayThread= println(s"current thread ${Thread.currentThread().getName}")
  }

}
