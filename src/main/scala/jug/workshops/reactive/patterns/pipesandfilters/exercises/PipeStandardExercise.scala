package jug.workshops.reactive.patterns.pipesandfilters.exercises

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

/**
  * Created by pawel on 01.10.16.
  */
object PipeStandardExercise {


  //1 - explain exercise
  //2 - show tests
  //3 - explain main
  def main(args: Array[String]): Unit = {
    val orderText = "(encryption)(certificate)<order id='123'>${orderData}</order>"
    val rawOrderBytes = TransportLib.pack(orderText)

    val system = ActorSystem("PipesAndFilters")

    val orderManagementSystem = system.actorOf(orderManagementProps())
    val deduplicator = system.actorOf(deduplicatorProps(orderManagementSystem))   //experiment : comment this out
    val authenticator = system.actorOf(authenticatorProps(deduplicator))
    val decrypter = system.actorOf(decrypterProps(authenticator))
    val endpoint = system.actorOf(endpointProps(decrypter))

    endpoint ! rawOrderBytes
    endpoint ! rawOrderBytes
    endpoint ! rawOrderBytes

    TimeUnit.SECONDS.sleep(1)
    system.terminate()
  }


  //Domain
  case class ProcessorIncomingOrder(content: Array[Byte])

  object ProcessorIncomingOrder {

    import TransportLib._

    def apply(text: String) = new ProcessorIncomingOrder(pack(text))
  }

  object TransportLib {
    def pack(content: String): Array[Byte] = content.toCharArray.map(_.toByte)
    def unpack(bytes: Array[Byte]): String = new String(bytes)
  }

  object OrderParser {
    def parseId(order: String): Option[String] = {
      val regex = "id='([0-9]+).*'".r

      val matched = regex findFirstMatchIn order
      matched.map(_.group(1))
    }
  }


  //Filters

  import TransportLib._

  def endpointProps(nextFilter: ActorRef): Props = Props(classOf[OrderAcceptanceEndpoint], nextFilter)
  def decrypterProps(nextFilter: ActorRef): Props = Props(classOf[Decrypter], nextFilter)
  def authenticatorProps(nextFilter: ActorRef): Props = Props(classOf[Authenticator], nextFilter)
  def deduplicatorProps(nextFilter: ActorRef): Props = Props(classOf[Deduplicator], nextFilter)
  def orderManagementProps(): Props = Props[OrderManagementSystem]


  class OrderAcceptanceEndpoint(nextFilter: ActorRef) extends Actor {
    override def receive = ???
  }

  class Decrypter(nextFilter: ActorRef) extends Actor {
    override def receive = ???
  }

  class Authenticator(nextFilter: ActorRef) extends Actor {
    override def receive = ???
  }

  //here will be more coding
  class Deduplicator(nextFilter: ActorRef) extends Actor {

    var processedOrderIds = Set[String]()

    override def receive = ???
  }


  class OrderManagementSystem extends Actor {
    override def receive: Receive = {
      case ProcessorIncomingOrder(order) =>
        println(s"RECEIVED ORDER ${unpack(order)}")
    }
  }

}


