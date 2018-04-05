package jug.workshops.reactive.akka.typed.patterns

import java.net.URI

import akka.actor.{Actor, ActorRef}

object DomainObject {

  case class ItemRef(id: URI)
  case class CustomerRef(id: URI)
  case class ShoppingCartRef(id: URI)


  //original
  case class ShoppingCart(items: Map[ItemRef, Int], owner: Option[CustomerRef]) {
    def setOwner(customer: CustomerRef): ShoppingCart = {
      require(owner.isEmpty, "owner cannot be overwritten")
      copy(owner = Some(customer))
    }

    def addItem(item: ItemRef, count: Int):ShoppingCart = {
      require(count > 0, s"count must be positive (trying to add $item with count $count)")

      val currentCount = items.getOrElse(item, 0)
        copy(items = items.updated(item, currentCount + count))
    }

    def removeItem(item: ItemRef, count: Int): ShoppingCart = {
      require(count > 0, s"count must be positive (trying to remove $item with count $count)")
      val currentCount = items.getOrElse(item, 0)
      val newCount = currentCount - count
      if (newCount <= 0) copy(items = items - item)
      else copy(items = items.updated(item, newCount))
    }

  }

  object ShoppingCart {
    val empty = ShoppingCart(Map.empty, None)
  }


  trait ShoppingCartMessage {
    def shoppingCart: ShoppingCartRef
  }


  sealed trait Command extends ShoppingCartMessage
  case class SetOwner(shoppingCart: ShoppingCartRef, owner: CustomerRef) extends Command
  case class AddItem(shoppingCart: ShoppingCartRef, item: ItemRef, count: Int) extends Command
  case class RemoveItem(shoppingCart: ShoppingCartRef, item: ItemRef, count: Int) extends Command

  sealed trait Query extends ShoppingCartMessage
  trait Querysealed
  case class GetItems(shoppingCart: ShoppingCartRef) extends Querysealed
  trait Event extends ShoppingCartMessage
  case class OwnerChanged(shoppingCart: ShoppingCartRef, owner: CustomerRef) extends Event
  case class ItemAdded(shoppingCart: ShoppingCartRef, item: ItemRef, count: Int) extends Event
  case class ItemRemoved(shoppingCart: ShoppingCartRef, item: ItemRef, count: Int) extends Event
  sealed trait Result extends ShoppingCartMessage
  case class GetItemsResult(shoppingCart: ShoppingCartRef, items: Map[ItemRef, Int]) extends Result



  case class ManagerCommand(cmd: Command, id: Long, replyTo: ActorRef)
  case class ManagerEvent(id: Long, event: Event)
  case class ManagerQuery(cmd: Query, id: Long, replyTo: ActorRef)
  case class ManagerResult(id: Long, result: Result)
  case class ManagerRejection(id: Long, reason: String)

  class Manager(var shoppingCart : ShoppingCart) extends Actor{

    /*
      * this is the usual constructor, the above allows priming with
      * previously persisted state.
    */
    def this() = this(ShoppingCart.empty)

    override def receive: Receive = {
      case ManagerCommand(cmd, id, replyTo) =>
        try{
          val event = executeCommand(cmd)
          replyTo ! event
        }catch{
          case ex:IllegalArgumentException =>
            replyTo ! ManagerRejection(id,ex.getMessage)
        }

      case ManagerQuery(cmd, id, replyTo) =>
        try{
          val result = cmd match {
            case GetItems(cart) => GetItemsResult(cart,shoppingCart.items)
          }
          replyTo ! ManagerResult(id,result)
        }catch {
          case ex:IllegalArgumentException =>
             replyTo ! ManagerRejection(id, ex.getMessage)
        }
    }

    private def executeCommand(cmd: Command) = cmd match {
        case SetOwner(card, owner) =>
          shoppingCart = shoppingCart.setOwner(owner)
          OwnerChanged(card, owner)
        case AddItem(cart, item, count) =>
          shoppingCart = shoppingCart.addItem(item, count)
          ItemAdded(cart, item, count)
        case RemoveItem(cart, item, count) =>
          shoppingCart = shoppingCart.removeItem(item, count)
          ItemRemoved(cart, item, count)
      }
  }

}
