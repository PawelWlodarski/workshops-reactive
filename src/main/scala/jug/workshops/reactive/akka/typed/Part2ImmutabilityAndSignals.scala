package jug.workshops.reactive.akka.typed

import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import jug.ThreadInfo
import jug.ThreadInfo._
import jug.workshops.reactive.akka.typed.Echo.EchoCommand
import jug.workshops.reactive.akka.typed.InjectionExample.{GetInfo, MockProductRepository}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object Part2ImmutabilityAndSignals {

  def main(args: Array[String]): Unit = {
//    echoExample()
    injectionExample()
  }


  def echoExample()={
    val main=Behaviors.setup[NotUsed]{ctx =>
      val printer: ActorRef[String] = ctx.spawn(Printer.behavior,"printer") //name mandatory!!!
      //show echo1 and echo2
      val echo: ActorRef[Echo.EchoCommand] = ctx.spawn(Echo.echo,"echo")

      println(s"main setup in thread $threadName")

      echo ! EchoCommand("immutable typed",printer)

      //race?
      Behaviors.stopped
    }

    val system=ActorSystem(main,"echoExample")

    wait3seconds(system)
  }

  def injectionExample() = {
    //create custom thread pool
    val pool=java.util.concurrent.Executors.newFixedThreadPool(2)
    val ec=ExecutionContext.fromExecutor(pool)

    //create repo dependency with custom thread pool
    val repo=new MockProductRepository(ec)

    val behavior=InjectionExample.initiateBehavior(repo)

    val main=Behaviors.setup[NotUsed]{ctx =>
      println(s"main setup in thread $threadName")
      val actor = ctx.spawn(behavior,"actorWithRepo")

      actor ! GetInfo(1)
      actor ! GetInfo(2)
      actor ! GetInfo(1)
      actor ! GetInfo(1)
      actor ! GetInfo(3)
      actor ! GetInfo(1)
      actor ! GetInfo(2)

      Behaviors.stopped
    }


    val system=ActorSystem(main,"injectionExample")
//    system.terminate()
    wait3seconds(system)
    pool.shutdown()
  }


  def wait3seconds[A](s:ActorSystem[A])={
    import scala.concurrent.duration._
    Await.result(s.whenTerminated,3 seconds)
  }

}

object Echo extends ThreadInfo{

  final case class EchoCommand(content: String, replyTp: ActorRef[String])

  import akka.actor.typed.scaladsl.ActorContext

  private[typed] val echoFunction: (ActorContext[EchoCommand], EchoCommand) => Behavior[EchoCommand] = { (ctx, cmd) =>
    cmd match {
      case EchoCommand(content, sender) =>
        println(s"echo function in thread $threadName")
        sender ! s"ECHO : $content"
        Behaviors.same
    }
  }

  private[typed] val echoFunction2: (ActorContext[EchoCommand], EchoCommand) => Behavior[EchoCommand] = {
    case (_, EchoCommand(content,sender)) =>
      println(s"echo function 2 with direct pattern matching in thread $threadName")
      sender ! s"ECHO : $content"
      Behaviors.same
  }

  val echo = Behaviors.immutable[EchoCommand](echoFunction)
}

object Printer{
  val behavior:Behavior[String]=Behaviors.immutable{(_,message) =>
    println(s"Printing $message in $threadName")
    Behaviors.same
  }
}


object InjectionExample {

  type ProductId = Int
  type Price=Double

  sealed trait AdminCommand
  final case class GetInfo(id:ProductId) extends AdminCommand


  case class Product(name:String,price:Double)

  //DEPENDECY INJECTION INTO BEHAVIOR
  def initiateBehavior(pr:ProductRepository):Behavior[AdminCommand] =
    Behaviors.immutable{
      case (ctx,GetInfo(id)) =>
        println(s"looking for product in actor , thread $threadName")
        pr.find(id).onComplete(findProductComplete)(ctx.executionContext) // CTX.EXECUTIONCONTEXT
        Behaviors.same
    }

  private def findProductComplete:Try[Product] => Unit = {
    case Success(p) => println(s"product found $p in thread : $threadName")
    case Failure(e) => println(s"product not found, exception=$e in thread : $threadName")
  }

  //SERVICE
  trait ProductRepository{
    def find(id:ProductId):Future[Product]
  }

  class MockProductRepository(ec:ExecutionContext) extends ProductRepository{
    private val products:Map[ProductId,Product] = Map(
      1 -> Product("Computer",999.0),
      2 -> Product("Mouse",20.5),
    )

    //ASYNC OPERATION
    override def find(id: ProductId): Future[Product] =
      Future{
        println(s"starting product search in thread $threadName")
        TimeUnit.MILLISECONDS.sleep(500)
        products(id)
      }(ec)
  }


}