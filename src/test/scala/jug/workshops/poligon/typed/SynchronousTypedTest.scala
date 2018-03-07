package jug.workshops.poligon.typed

import akka.actor.typed._
import akka.actor.typed.scaladsl._
import akka.testkit.typed.scaladsl.Effects._
import akka.testkit.typed.scaladsl._
import org.scalatest.FunSuite


class SynchronousTypedTest extends FunSuite{

  import ActorForTests._


  test("Synchronous tests "){
    val testkit = BehaviorTestKit(myBehavior)

    testkit.run(CreateChild("child"))
    testkit.expectEffect(Spawned(childActor,"child"))

    testkit.run(CreateAnonymousChild)
    testkit.expectEffect(SpawnedAnonymous(childActor))


    val inbox=TestInbox[String]()
    testkit.run(SayHello(inbox.ref))
    inbox.expectMessage("hello")


    testkit.run(SayHelloToChild("child2"))
    val childInbox=testkit.childInbox[String]("child2")
    childInbox.expectMessage("hello")

    testkit.run(SayHelloToAnonymousChild)
    val childInbox2=testkit.childInbox[String]("$b")
    childInbox2.expectMessage("hello stranger")

  }


}


object ActorForTests {
  sealed trait CmdForTesting
  case object CreateAnonymousChild extends CmdForTesting
  case class CreateChild(childName: String) extends CmdForTesting
  case class SayHelloToChild(childName: String) extends CmdForTesting
  case object SayHelloToAnonymousChild extends CmdForTesting
  case class SayHello(who: ActorRef[String]) extends CmdForTesting

  val childActor = Behaviors.immutable[String]{(_,_) =>
    Behaviors.same[String]
  }

  val myBehavior = Behaviors.immutablePartial[CmdForTesting]{
    case (ctx, CreateChild(name)) ⇒
      ctx.spawn(childActor, name)
      Behaviors.same
    case (ctx, CreateAnonymousChild) ⇒
      ctx.spawnAnonymous(childActor)
      Behaviors.same
    case (ctx, SayHelloToChild(childName)) ⇒
      val child: ActorRef[String] = ctx.spawn(childActor, childName)
      child ! "hello"
      Behaviors.same
    case (ctx, SayHelloToAnonymousChild) ⇒
      val child: ActorRef[String] = ctx.spawnAnonymous(childActor)
      child ! "hello stranger"
      Behaviors.same
    case (_, SayHello(who)) ⇒
      who ! "hello"
      Behaviors.same
  }
}
