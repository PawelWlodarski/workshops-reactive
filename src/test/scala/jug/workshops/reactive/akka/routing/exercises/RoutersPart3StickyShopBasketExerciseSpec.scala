package jug.workshops.reactive.akka.routing.exercises

import akka.actor.{ActorSystem, Props}
import akka.routing.ConsistentHashingPool
import akka.testkit.{ImplicitSender, TestKit}
import common.StopSystemAfterAll
import jug.workshops.reactive.akka.routing.exercises.RoutersPart3StickyShopBasketExercise._
import org.scalatest.{MustMatchers, WordSpecLike}


class RoutersPart3StickyShopBasketExerciseSpec extends TestKit(ActorSystem()) with MustMatchers
    with WordSpecLike with StopSystemAfterAll with ImplicitSender{


    "Show Basket" should {
      "Store purchased products" in {
        val basket = system.actorOf(Props[ShopBasket])


        val tv = ShopProductRouting("tv", BigDecimal("100"))
        val pc = ShopProductRouting("pc", BigDecimal("80"))
        val book = ShopProductRouting("book", BigDecimal("20"))

        basket ! Request(1,Purchase(tv))
        basket ! Request(1,Purchase(pc))
        basket ! Request(1,Purchase(book))


        basket ! Request(1,ListProducts)
        import scala.collection._
        expectMsg(Response(200,Some(immutable.Seq(tv,pc,book))))

      }
    }

    "Shop router" should {
      "route purchase to proper worker" in {

        val basket = system.actorOf(
          ConsistentHashingPool(3,hashMapping = ShopBasket.hashingFunction).props(Props[ShopBasket])
        )

        val tv = ShopProductRouting("tv", BigDecimal("100"))
        val pc = ShopProductRouting("pc", BigDecimal("80"))
        val book = ShopProductRouting("book", BigDecimal("20"))
        val car = ShopProductRouting("car", BigDecimal("200"))
        val mp3player = ShopProductRouting("mp3player", BigDecimal("50"))

        basket ! Request(1,Purchase(tv))
        basket ! Request(2,Purchase(pc))
        basket ! Request(3,Purchase(book))
        basket ! Request(2,Purchase(car))
        basket ! Request(3,Purchase(mp3player))

        basket ! Request(1,ListProducts)
        basket ! Request(2,ListProducts)
        basket ! Request(3,ListProducts)

        import scala.collection._
        expectMsg(Response(200,Some(immutable.Seq(tv))))
        expectMsg(Response(200,Some(immutable.Seq(pc,car))))
        expectMsg(Response(200,Some(immutable.Seq(book, mp3player))))

      }
    }

}
