package jug.workshops.reactive.akka.intro

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import org.scalatest.{MustMatchers, WordSpecLike}

class SingleActorSyncTest extends TestKit(ActorSystem("test")) with  WordSpecLike with MustMatchers{

  "SingleActor" must {
    "accumulate state" in {
      import SomeActor._
      val actorRef=TestActorRef[SomeActor]
      println("testThread : "+Thread.currentThread().getName)
      actorRef ! Append(1)
      actorRef ! Append(2)
      actorRef ! Append(3)
      actorRef ! Append(4)

      actorRef.underlyingActor.state must contain only(1,2,3,4)
    }
  }
}