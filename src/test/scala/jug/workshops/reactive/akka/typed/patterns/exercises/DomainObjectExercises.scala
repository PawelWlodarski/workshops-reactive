package jug.workshops.reactive.akka.typed.patterns.exercises

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.testkit.{TestKit, TestProbe}
import akka.testkit.typed.scaladsl.{BehaviorTestKit, TestInbox}
import org.scalatest.{BeforeAndAfterAll, FunSuite, FunSuiteLike, MustMatchers}


//EXERCISES
// finish DomainPurchase1
// finish purchaseBehavior
// finish Untyped actor
object DomainObjectExercises {

  class DomainPurchase1 private() {
    private var products = List[DomainProduct1]()

    //max 3 products
    def addProduct(p: DomainProduct1): DomainResult = ???



    def removeProduct(name: String): Unit = ???

    def calculateFinalPrice(): Double = ???

    private def calculateDiscount(totalPrice: Double) = ???


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

    def mapAddProduct(p:DomainProduct1) : DomainEvent1 = ???


    Behaviors.immutable[DomainCommand1] { (_, msg) =>
      msg match {
        case AddProduct1(p, replyTo) => replyTo ! mapAddProduct(p)
        case RemoveProduct1(name,replyTo) => ???
        case Checkout1(replyTo) => ???
      }
      Behaviors.same
    }

  }
}

  import DomainObjectExercises._

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
        val stateActor =  BehaviorTestKit(DomainObjectExercises.purchaseBehavior)
        val senderBox=TestInbox[DomainEvent1]()
        val product = DomainProduct1("testProduct",69)

        stateActor.run(AddProduct1(product,senderBox.ref))

        senderBox.expectMessage(ProductAdded1("testProduct"))
    }

    test("fail to add product"){
      val stateActor =  BehaviorTestKit(DomainObjectExercises.purchaseBehavior)
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
      val stateActor =  BehaviorTestKit(DomainObjectExercises.purchaseBehavior)
      val senderBox=TestInbox[DomainEvent1]()
      val product = DomainProduct1("testProduct",69)

      stateActor.run(AddProduct1(product,senderBox.ref))

      senderBox.receiveAll()

      stateActor.run(RemoveProduct1("testProduct",senderBox.ref))
      senderBox.expectMessage(ProductRemoved1("testProduct"))
    }

    test("checkout"){
      val stateActor =  BehaviorTestKit(DomainObjectExercises.purchaseBehavior)
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
  //EXERCISE !!!
  class Untyped(listener:ActorRef, shop:akka.actor.typed.ActorRef[DomainCommand1]) extends akka.actor.Actor{
    override def receive: Receive = ???
  }


  test("untyped to typed"){
    val probe = TestProbe()
    val shop=system.spawn(DomainObjectExercises.purchaseBehavior,"candyShop")
    val untyped=system.actorOf(Props(new Untyped(probe.ref,shop)))

    untyped ! "GO"

    probe.expectMsg(PurchaseEvent1(80.0))

  }

  override protected def afterAll(): Unit = system.terminate()
}


