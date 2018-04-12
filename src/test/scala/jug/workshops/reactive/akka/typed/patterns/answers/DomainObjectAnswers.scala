package jug.workshops.reactive.akka.typed.patterns.answers

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.testkit.{TestKit, TestProbe}
import akka.testkit.typed.scaladsl.{ActorTestKit, BehaviorTestKit, TestInbox}
import org.scalatest.{BeforeAndAfterAll, FunSuite, FunSuiteLike, MustMatchers}

object DomainObjectAnswers {

  //max 3 products
  //
  class DomainPurchase1 private() {
    private var products = List[DomainProduct1]()

    def addProduct(p: DomainProduct1): DomainResult =
      if (products.size < 3) {
        products = p :: products
        DomainSuccess
      } else DomainFailure


    def removeProduct(name: String): Unit =
      products = products.filter(_.name != name)

    def calculateFinalPrice(): Double = {
      val price = products.map(_.price).sum
      calculateDiscount(price)
    }

    private def calculateDiscount(totalPrice: Double) =
      if (totalPrice > DomainPurchase1.discountThreeshold)
        totalPrice * (100 - DomainPurchase1.discountPercent) / 100
      else
        totalPrice

  }

  object DomainPurchase1 {
    val discountThreeshold = 50
    val discountPercent = 20

    def apply(): DomainPurchase1 = new DomainPurchase1()
  }


  case class DomainProduct1(name: String, price: Double)

  sealed trait DomainResult

  case object DomainSuccess extends DomainResult

  case object DomainFailure extends DomainResult


  sealed trait DomainCommand1

  case class AddProduct1(p: DomainProduct1, replyTo: akka.actor.typed.ActorRef[DomainEvent1]) extends DomainCommand1

  case class RemoveProduct1(n: String, replyTo: akka.actor.typed.ActorRef[DomainEvent1]) extends DomainCommand1

  case class Checkout1(replyTo: akka.actor.typed.ActorRef[PurchaseEvent1]) extends DomainCommand1

  sealed trait DomainEvent1

  case class ProductAdded1(name: String) extends DomainEvent1

  case class DomainError1(content: String) extends DomainEvent1

  case class ProductRemoved1(content: String) extends DomainEvent1

  case class PurchaseEvent1(total: Double)


  val purchaseBehavior: Behavior[DomainCommand1] = Behaviors.setup { _ =>

    val purchaseState = DomainPurchase1()

    def mapAddProduct(p:DomainProduct1) : DomainEvent1 = purchaseState.addProduct(p) match {
      case DomainSuccess => ProductAdded1(p.name)
      case DomainFailure => DomainError1(s"error : ${p.name}")
    }

    def mapRemoveProduct(n:String): ProductRemoved1 = {
      purchaseState.removeProduct(n)
      ProductRemoved1(n)
    }

    def mapCheckout() : PurchaseEvent1 = {
      val price = purchaseState.calculateFinalPrice()
      PurchaseEvent1(price)
    }


    Behaviors.immutable[DomainCommand1] { (_, msg) =>
      msg match {
        case AddProduct1(p, replyTo) => replyTo ! mapAddProduct(p)
        case RemoveProduct1(name,replyTo) => replyTo ! mapRemoveProduct(name)
        case Checkout1(replyTo) => replyTo ! mapCheckout()
      }
      Behaviors.same
    }

  }
}

  import DomainObjectAnswers._

  class DomainPurchase1UnitTest extends FunSuite with MustMatchers {
    test("calculate price") {
      val purchase = DomainPurchase1()

      purchase.addProduct(DomainProduct1("keyboard", 20))
      purchase.addProduct(DomainProduct1("mouse", 10))

      purchase.calculateFinalPrice() mustBe 30.0

      purchase.addProduct(DomainProduct1("computer", 70))

      //with discount
      purchase.calculateFinalPrice() mustBe 80.0
    }

    test("max 3 products") {
      val purchase = DomainPurchase1()
      purchase.addProduct(DomainProduct1("keyboard", 20)) mustBe DomainSuccess
      purchase.addProduct(DomainProduct1("accessory 1", 10)) mustBe DomainSuccess
      purchase.addProduct(DomainProduct1("accessory 2", 10)) mustBe DomainSuccess
      purchase.addProduct(DomainProduct1("accessory 3", 10)) mustBe DomainFailure
    }

    test("remove product") {
      val purchase = DomainPurchase1()
      purchase.addProduct(DomainProduct1("keyboard", 20)) mustBe DomainSuccess
      purchase.addProduct(DomainProduct1("mouse", 10)) mustBe DomainSuccess
      purchase.addProduct(DomainProduct1("computer", 70)) mustBe DomainSuccess

      purchase.calculateFinalPrice() mustBe 80.0

      purchase.removeProduct("computer")

      purchase.calculateFinalPrice() mustBe 30.0
      purchase.addProduct(DomainProduct1("headphones", 25))
      purchase.calculateFinalPrice() mustBe 44.0
    }
  }


  class DomainBehavior1SyncTest extends FunSuite with MustMatchers{

    test("add product"){
        val stateActor =  BehaviorTestKit(DomainObjectAnswers.purchaseBehavior)
        val senderBox=TestInbox[DomainEvent1]()
        val product = DomainProduct1("testProduct",69)

        stateActor.run(AddProduct1(product,senderBox.ref))

        senderBox.expectMessage(ProductAdded1("testProduct"))
    }

    test("fail to add product"){
      val stateActor =  BehaviorTestKit(DomainObjectAnswers.purchaseBehavior)
      val senderBox=TestInbox[DomainEvent1]()
      val product = DomainProduct1("testProduct",69)

      stateActor.run(AddProduct1(product,senderBox.ref))
      stateActor.run(AddProduct1(product,senderBox.ref))
      stateActor.run(AddProduct1(product,senderBox.ref))

      senderBox.receiveAll()

      stateActor.run(AddProduct1(product,senderBox.ref))
      senderBox.expectMessage(DomainError1("error : testProduct"))
    }

    test("remove product"){
      val stateActor =  BehaviorTestKit(DomainObjectAnswers.purchaseBehavior)
      val senderBox=TestInbox[DomainEvent1]()
      val product = DomainProduct1("testProduct",69)

      stateActor.run(AddProduct1(product,senderBox.ref))

      senderBox.receiveAll()

      stateActor.run(RemoveProduct1("testProduct",senderBox.ref))
      senderBox.expectMessage(ProductRemoved1("testProduct"))
    }

    test("checkout"){
      val stateActor =  BehaviorTestKit(DomainObjectAnswers.purchaseBehavior)
      val senderBox=TestInbox[Any]()
      val product1 = DomainProduct1("testProduct1",10)
      val product2 = DomainProduct1("testProduct2",20)
      val product3 = DomainProduct1("testProduct3",70)

      stateActor.run(AddProduct1(product1,senderBox.ref))
      stateActor.run(AddProduct1(product2,senderBox.ref))
      senderBox.receiveAll()
      stateActor.run(Checkout1(senderBox.ref))
      senderBox.expectMessage(PurchaseEvent1(30.0))

      stateActor.run(AddProduct1(product3,senderBox.ref))
      senderBox.receiveAll()
      stateActor.run(Checkout1(senderBox.ref))
      senderBox.expectMessage(PurchaseEvent1(80.0))

    }
  }

class DomainBehavior1AsyncTest extends TestKit(ActorSystem("tests")) with FunSuiteLike with MustMatchers with BeforeAndAfterAll {

  import akka.actor.typed.scaladsl.adapter._
  class Untyped(listener:ActorRef, shop:akka.actor.typed.ActorRef[DomainCommand1]) extends akka.actor.Actor{
    override def receive: Receive = {
      case "GO" =>
        val product1 = DomainProduct1("testProduct1",10)
        val product2 = DomainProduct1("testProduct2",20)
        val product3 = DomainProduct1("testProduct3",70)

        shop ! AddProduct1(product1,self)
        shop ! AddProduct1(product2,self)
        shop ! AddProduct1(product3,self)
        shop ! Checkout1(self)
      case msg:PurchaseEvent1 =>
        listener ! msg
    }
  }


  test("untyped to typed"){
    val probe = TestProbe()
    val shop=system.spawn(DomainObjectAnswers.purchaseBehavior,"candyShop")
    val untyped=system.actorOf(Props(new Untyped(probe.ref,shop)))

    untyped ! "GO"

    probe.expectMsg(PurchaseEvent1(80.0))

  }

  override protected def afterAll(): Unit = system.terminate()
}


