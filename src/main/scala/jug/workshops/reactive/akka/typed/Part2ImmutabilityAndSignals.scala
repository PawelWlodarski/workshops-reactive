package jug.workshops.reactive.akka.typed

import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, PostStop}
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout
import jug.ThreadInfo
import jug.ThreadInfo._
import jug.workshops.reactive.akka.typed.Echo.EchoCommand
import jug.workshops.reactive.akka.typed.InjectionExample.{GetInfo, MockProductRepository}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object Part2ImmutabilityAndSignals {

  def main(args: Array[String]): Unit = {
    echoExample()
//    injectionExample()
//    stateExample()
  }


  /**
    *
    * * First example shows simple actor which just repeat message
    * * Two actor are used and an explicit sender
    * * Behaviors.setup
    * * Spawning typed actors
    * * Stoping typed actors
    */
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

  /**
    * * injecting dependencies into typed actor
    * * using different thread pools for blocking async operations
    * * starting async operations inside an actor
    *
    */
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

  /**
    *  * managing swtate in immutable behaviors
    *  * typed ask pattern
    *  * signals
    */
  import StateExample._
  import akka.actor.typed.scaladsl.AskPattern._
  import scala.concurrent.duration._
  def stateExample() = {
    //typed system
    val system = ActorSystem[StateProtocol](stateBehavior(0),"stateExample")

    //create custom thread pool
    val pool=java.util.concurrent.Executors.newFixedThreadPool(2)
    implicit val ec=ExecutionContext.fromExecutor(pool)
    implicit val timeout:Timeout=1 second
    implicit val s = system.scheduler



    val firstResponse: Future[Int] = system ? { (ref: ActorRef[Int]) => IncrementState(ref)}

    for{
      first <- firstResponse
      second <- system ? { (ref: ActorRef[Int]) => IncrementState(ref)}
      third <- system ? { (ref: ActorRef[Int]) => IncrementState(ref)}
      fourth <- system ? { (ref: ActorRef[Int]) => DecrementState(ref)}
      fifth <- system ? { (ref: ActorRef[Int]) => IncrementState(ref)}

    } {
      println(s"first result in $threadName : $first")
      println(s"second result in $threadName : $second")
      println(s"third result in $threadName : $third")
      println(s"fourth result in $threadName : $fourth")
      println(s"fifth result in $threadName : $fifth")
      system.terminate()
      pool.shutdown()
    }


    wait3seconds(system)
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

  val echo = Behaviors.receive[EchoCommand](echoFunction)
}

object Printer{
  val behavior:Behavior[String]=Behaviors.receive{(_,message) =>
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
    Behaviors.receive{
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


object StateExample{

  trait StateProtocol{
    def replyTo:ActorRef[Int]
  }
  final case class IncrementState(replyTo:ActorRef[Int]) extends StateProtocol
  final case class DecrementState(replyTo:ActorRef[Int]) extends StateProtocol

  //show where state inference is failing
  def stateBehavior(init:Int=0):Behavior[StateProtocol]= Behaviors.receive[StateProtocol]{ (_, message) =>
    val newState=calculateNewState(init,message)
    message.replyTo ! newState
    stateBehavior(newState)
  }.receiveSignal{ //sho Signal
    case (_,PostStop) =>
      println(s"state behavior stopped in $threadName")
      Behaviors.stopped
  }

  private def calculateNewState(init:Int,message:StateProtocol):Int = message match {
    case IncrementState(_) =>
      println(s"incrementing state in $threadName")
      init + 1
    case DecrementState(_) =>
      println(s"decrementing state in $threadName")
      init - 1
  }
}