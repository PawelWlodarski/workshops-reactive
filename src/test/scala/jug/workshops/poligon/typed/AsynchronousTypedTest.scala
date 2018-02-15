package jug.workshops.poligon.typed

import akka.actor.typed.scaladsl.Actor
import akka.actor.typed.{ActorRef, Behavior}
import akka.testkit.typed.TestKit
import akka.testkit.typed.scaladsl.TestProbe
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

class AsynchronousTypedTest extends TestKit("Basic_testing_spec") with FunSuiteLike with BeforeAndAfterAll{

  import Protocol._

  test("asynchronous tests"){
      val probe = TestProbe[AsynTestPong]()

      val pinger = spawn(echoActor)

      pinger ! AsynTestPing("hello", probe.ref)

      probe.expectMsg(AsynTestPong("hello"))
  }

  override protected def afterAll(): Unit = shutdown()
}

object Protocol{
  case class AsynTestPing(msg: String, response: ActorRef[AsynTestPong])
  case class AsynTestPong(msg: String)


  val echoActor: Behavior[AsynTestPing] = Actor.immutable[AsynTestPing]{ (_, msg)=>
    msg match {
      case AsynTestPing(m,replyTo) =>
        replyTo ! AsynTestPong(m)
        Actor.same
    }
  }
}

