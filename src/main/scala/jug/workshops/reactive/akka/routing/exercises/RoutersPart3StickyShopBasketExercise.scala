package jug.workshops.reactive.akka.routing.exercises

import akka.actor.Actor
import akka.routing.ConsistentHashingRouter.ConsistentHashMapping

object RoutersPart3StickyShopBasketExercise {


  sealed trait ShopAction
  final case class Purchase(product:ShopProductRouting) extends ShopAction
  final case object ListProducts extends ShopAction

  case class ShopProductRouting(name:String, price:BigDecimal)

  case class Request(sessionId:Int,action:ShopAction)
  case class Response[A](code:Int,content:A)

  object ShopBasket{
    def hashingFunction:ConsistentHashMapping = ???
  }


  class ShopBasket extends Actor{

    val Seq=scala.collection.immutable.Seq
    type Seq[A]=scala.collection.immutable.Seq[A]
    var purchased=Map[Int,Seq[ShopProductRouting]]()

    override def receive: Receive = {
      case _ => ???
    }
  }




}
