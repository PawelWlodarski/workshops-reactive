package jug.workshops.reactive.akka.basics.answers

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import common.StopSystemAfterAll
import jug.workshops.reactive.akka.basics.exercises.BasicsPart7MapReduceExercise.JobDivisionProtocol.{ComputationTask, Result, SendResponse}
import org.scalatest.{MustMatchers, WordSpecLike}

/**
  * Created by pawel on 30.10.16.
  */
class BasicsPart7MapReduce extends TestKit(ActorSystem("test")) with MustMatchers
    with WordSpecLike with StopSystemAfterAll with ImplicitSender{

  import jug.workshops.reactive.akka.basics.exercises.BasicsPart7MapReduceExercise._

  "Processing Logic" should{
    "calculate sum" in {
      val expectedSum=(10 until 100).sum

      ProcessingLogic.computation(10,100) mustBe expectedSum
    }
  }

  "Task Processor" should {
    "send response to combiner" in {
      val probe=TestProbe()

      val taskProcessor=system.actorOf(Props(new TaskProcessor(probe.ref)))

      taskProcessor ! ComputationTask(10,100)

      probe.expectMsg(Result(4905))
    }
  }

  "Combiner" should {
    "combine results" in {
      val probe=TestProbe()

      val combiner=system.actorOf(Props[Combiner])

      combiner ! Result(1000)
      combiner ! Result(2000)
      combiner ! Result(3000)

      combiner ! SendResponse(probe.ref)

      probe.expectMsg(6000)
    }
  }

  "Final Simulation" should {
    "computer results from different tasks" in {
      val probe=TestProbe()

      val combiner=system.actorOf(Props[Combiner])

      val taskProcessors=(1 to 4)
        .map(_=>Props(new TaskProcessor(combiner)))
        .map(props=>system.actorOf(props))


      taskProcessors(0) ! ComputationTask(1,5)
      taskProcessors(1) ! ComputationTask(5,10)
      taskProcessors(2) ! ComputationTask(10,15)
      taskProcessors(3) ! ComputationTask(15,25)

      TimeUnit.MILLISECONDS.sleep(100)

      combiner ! SendResponse(probe.ref)
      probe.expectMsg(300)
    }
  }


}

