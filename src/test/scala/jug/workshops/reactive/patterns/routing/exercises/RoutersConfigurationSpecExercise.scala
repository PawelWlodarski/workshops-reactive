package jug.workshops.reactive.patterns.routing.exercises

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import common.StopSystemAfterAll
import jug.workshops.reactive.patterns.routing.exercises.RoutersConfigurationExercise._
import org.scalatest.{MustMatchers, WordSpecLike}

/**
  * Created by pawel on 23.10.16.
  */
class RoutersConfigurationSpecAnswer extends TestKit(RoutersConfiguration.testSystem) with WordSpecLike
  with MustMatchers with StopSystemAfterAll with ImplicitSender {

  val endPoint = system.actorOf(Props[MathEndPoint], "exerciseActor")

  //group workers
  system.actorOf(Props[ExerciseGroup], "exerciseGroup")

  "Router configuration exercise " should {
    "finish add job within 500 milliseconds [Pool]" in {

      import scala.concurrent.duration._

      endPoint ! AddOperation(1 to 3)
      endPoint ! AddOperation(1 to 5)
      endPoint ! AddOperation(1 to 6)

      val result1 = expectMsgPF(600 milliseconds) { case MathResponse(_, result) => result }
      val result2 = expectMsgPF(600 milliseconds) { case MathResponse(_, result) => result }
      val result3 = expectMsgPF(600 milliseconds) { case MathResponse(_, result) => result }

      Seq(result1, result2, result3) must contain only(6, 15, 21)

    }
  }



  "finish add job within 500 milliseconds [Group] " in {

    import scala.concurrent.duration._

    endPoint ! PowOperation(2, 2)
    endPoint ! PowOperation(2, 3)
    endPoint ! PowOperation(2, 4)

    val result1 = expectMsgPF(600 milliseconds) { case MathResponse(_, result) => result }
    val result2 = expectMsgPF(600 milliseconds) { case MathResponse(_, result) => result }
    val result3 = expectMsgPF(600 milliseconds) { case MathResponse(_, result) => result }

    Seq(result1, result2, result3) must contain only(4, 8, 16)

  }

    "finish add job within 500 milliseconds [Mix]" in {

      import scala.concurrent.duration._

      endPoint ! PowOperation(2, 2)
      endPoint ! AddOperation(1 to 5)
      endPoint ! PowOperation(2, 4)

      val result1 = expectMsgPF(600 milliseconds) { case MathResponse(_, result) => result }
      val result2 = expectMsgPF(600 milliseconds) { case MathResponse(_, result) => result }
      val result3 = expectMsgPF(600 milliseconds) { case MathResponse(_, result) => result }

      Seq(result1, result2, result3) must contain only(4, 15, 16)

    }

}

object RoutersConfiguration {

  def testSystem: ActorSystem = {
    val demoConfig = ConfigFactory.load("routers/exercise")
    ActorSystem("routersExercise", demoConfig)
  }


}
