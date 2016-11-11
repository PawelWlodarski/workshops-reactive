package jug.workshops.reactive.akka.routing.exercises

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import common.StopSystemAfterAll
import org.scalatest.{MustMatchers, WordSpecLike}
import jug.workshops.reactive.akka.routing.exercises.Part1RoutersCustomRoundRobinRouterExercise._

import scala.collection.immutable.{IndexedSeq, Seq}

class Part1RoutersCustomRoundRobinRouterSpecExercise extends TestKit(ActorSystem("customRouter"))
with WordSpecLike with MustMatchers with StopSystemAfterAll with ImplicitSender{


  "Custom Round Robin Router" should {
    "route messages to workers in cycles" in {
      val workerProps=Props[CustomWorker]
      val router=system.actorOf(Props(new CustomRouter(5,workerProps)),"customRouter")

      (1 to 10).foreach{_=>
        router ! RoutedJob("someText")
      }

      val results: Seq[AnyRef] =receiveN(10)

      results
        .groupBy(identity)
        .mapValues(_.size).toSeq  must contain allOf(
        "SOMETEXTcustomRouter:worker:1" -> 2,
        "SOMETEXTcustomRouter:worker:2" -> 2,
        "SOMETEXTcustomRouter:worker:3" -> 2,
        "SOMETEXTcustomRouter:worker:4" -> 2,
        "SOMETEXTcustomRouter:worker:5" -> 2
        )
    }
  }

  "Cycle" should {
    "generate cyclic iterator" in {
      val cycle=Cycle.cycleBetween(0,4)

      val result: IndexedSeq[Int] =(1 to 8).map(_ => cycle.next)

      result mustBe  IndexedSeq(0, 1, 2, 3, 0, 1, 2, 3)

    }
  }

}
