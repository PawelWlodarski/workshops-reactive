package jug.workshops.reactive.akka.routing.answers

import akka.actor.Actor
import akka.routing.ConsistentHashingRouter.ConsistentHashMapping

object RoutersPart3StickyShopBasketAnswer {


  sealed trait ShopAction
  final case class Purchase(product:ShopProductRouting) extends ShopAction
  final case object ListProducts extends ShopAction

  case class ShopProductRouting(name:String, price:BigDecimal)

  case class Request(sessionId:Int,action:ShopAction)
  case class Response[A](code:Int,content:A)

  object ShopBasket{
    def hashingFunction:ConsistentHashMapping={
      case Request(sessionId,_) => sessionId
    }
  }


  class ShopBasket extends Actor{

    val Seq=scala.collection.immutable.Seq
    type Seq[A]=scala.collection.immutable.Seq[A]
    var purchased=Map[Int,Seq[ShopProductRouting]]()

    override def receive: Receive = {
      case Request(sessionId,Purchase(product)) =>
        val purchasedPerSession=purchased.getOrElse(sessionId,Seq.empty[ShopProductRouting])
        val updatedPurchases=purchasedPerSession :+ product
        purchased = purchased + (sessionId -> updatedPurchases)
      case Request(sessionId,ListProducts) => sender ! Response(200,purchased.get(sessionId))
    }
  }




}
