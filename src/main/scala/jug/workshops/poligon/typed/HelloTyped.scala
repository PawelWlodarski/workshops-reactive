package jug.workshops.poligon.typed

import java.util.concurrent.TimeUnit

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._


object HelloTyped {


  def main(args: Array[String]): Unit = {
//    Hello1.hello1()
    Hello2.hello()
  }


  object Greeter1 {

    sealed trait Command

    case object Greet extends Command

    final case class WhoToGreet(who: String) extends Command

    val greeterBehaviour: Behavior[Command] = Behaviors.mutable[Command](ctx => new Greeter1)
  }

  class Greeter1 extends Behaviors.MutableBehavior[Greeter1.Command] {
    import Greeter1._
    private var greeting = "hello"

    override def onMessage(msg: Greeter1.Command): Behavior[Greeter1.Command] = {
      msg match {
        case WhoToGreet(who) => greeting = s"hello, $who"
        case Greet => println(greeting)
      }
      this
    }
  }

  object Greeter2 {

    sealed trait Command

    case object Greet extends Command

    final case class WhoToGreet(who: String) extends Command

    val greeterBehaviour: Behavior[Command] = greeterBehavior(currentGreeting = "hello")

    private def greeterBehavior(currentGreeting: String): Behavior[Command] =
      Behaviors.immutable[Command] { (ctx, msg) =>
        msg match  {
          case WhoToGreet(who) =>
            greeterBehavior(s"hello, $who")
          case Greet =>
            println(currentGreeting)
            Behaviors.same
        }
      }
  }

  object Hello2 {

    def hello()= {
      val root = Behaviors.setup[Nothing]{ctx =>
        import Greeter2._

        val greeter:ActorRef[Command] = ctx.spawn(greeterBehaviour, "greeter")
        greeter ! WhoToGreet("World2")
        greeter ! Greet
        Behaviors.empty
      }
      val system = ActorSystem[Nothing](root,"HelloWorld")
      TimeUnit.SECONDS.sleep(1)
      system.terminate()
    }
  }


  object Hello1 {

    final case class Greet(whom: String, replyTo: ActorRef[Greeted])

    final case class Greeted(whom: String)


    val greeter: Behavior[Greet] = Behaviors.immutable[Greet] { (_, msg) =>
      println(s"Hello ${msg.whom}")
      msg.replyTo ! Greeted(msg.whom)
      Behaviors.same
    }

    def hello1() = {
      val system: ActorSystem[Greet] = ActorSystem(greeter, "hello")
      implicit val timeout: Timeout = 5 seconds
      implicit val s = system.scheduler


      val sending: Future[Greeted] = system ? ((actor: ActorRef[Greeted]) => Greet("world", actor))
      //    system ? (Greet("world", _ ))


      for {
        greeting <- sending.recover { case ex => ex.getMessage }
        _ <- {
          println(s"result: $greeting");
          system.terminate()
        }
      } println("system terminated")
    }
  }


}
