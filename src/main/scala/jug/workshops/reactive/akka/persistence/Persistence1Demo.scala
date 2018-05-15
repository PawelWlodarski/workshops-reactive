package jug.workshops.reactive.akka.persistence

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.PersistentActor
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import jug.workshops.reactive.akka.persistence.PersistenceDemo1.PersistentExample1.{AddItem, GetItems, Items}

import scala.concurrent.Await

object PersistenceDemo1 {

  val persistenceConfig = ConfigFactory.load("persistence/persistenceworkshops")

  def main(args: Array[String]): Unit = {
      example1()
  }


  def example1() = {
    import akka.pattern.ask
    val system=ActorSystem("persistentExample1System",persistenceConfig)
    val uniqueId=7

    val persistentActor1 = system.actorOf(Props(new PersistentExample1(uniqueId)),s"persistentActor$uniqueId")

    persistentActor1 ! AddItem("item1")
    persistentActor1 ! AddItem("item2")
    persistentActor1 ! AddItem("item3")

    import scala.concurrent.duration._
    implicit val timeout:Timeout = 1 second

    val processingFuture=(persistentActor1 ? GetItems).mapTo[Items]

    val firstCall=Await.result(processingFuture, 2 second)

    println(firstCall)


    system.terminate()

  }


  def example2() = {

  }



  class PersistentExample1(unique:Int) extends PersistentActor with ActorLogging{
    import PersistentExample1._
    var state=List.empty[String]

    override def persistenceId: String = "Persistent_Example_1"+unique

    override def receiveRecover: Receive = {
      case ItemAdded(item) =>
        updatestate(item)
        log.info("recovered item : {}",item)
    }

    override def receiveCommand: Receive = {
      case AddItem(item) =>
        updatestate(item)
        persist(ItemAdded(item)){ evt =>
            log.info("event persisted {}", evt)
        }
      case GetItems =>
        sender() ! Items(state)

    }

    private def updatestate(item:String) = state=item :: state

  }

  object PersistentExample1{
    trait Command
    case class AddItem(item:String) extends Command
    case object GetItems extends Command

    case class ItemAdded(item:String)
    case class Items(is:List[String])
  }

  object BusinessExample{
    trait BusinessEvent
    case class AddProduct(item:String) extends BusinessEvent
    case object RemoveProducts extends BusinessEvent
  }

  class BusinessState{
    private var products=Set.empty[String]
  }

  class EventStoreExample1(unique:Int) extends PersistentActor with ActorLogging{
    override def persistenceId: String = s"event_store1$unique"

    override def receiveRecover: Receive = ???

    override def receiveCommand: Receive = ???

  }
}


