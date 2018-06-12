package jug.workshops.poligon.typed

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

class AsynchronousTypedTest extends ActorTestKit with FunSuiteLike with BeforeAndAfterAll{

  import Protocol._

  test("asynchronous tests"){
      val probe = TestProbe[AsynTestPong]()

      val pinger = spawn(echoActor)

      pinger ! AsynTestPing("hello", probe.ref)

      probe.expectMessage(AsynTestPong("hello"))
  }

  override protected def afterAll(): Unit = shutdownTestKit()
}

object Protocol{
  case class AsynTestPing(msg: String, response: ActorRef[AsynTestPong])
  case class AsynTestPong(msg: String)


  val echoActor: Behavior[AsynTestPing] = Behaviors.receive[AsynTestPing]{ (_, msg)=>
    msg match {
      case AsynTestPing(m,replyTo) =>
        replyTo ! AsynTestPong(m)
        Behaviors.same
    }
  }
}

