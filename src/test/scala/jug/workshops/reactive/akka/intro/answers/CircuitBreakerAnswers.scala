package jug.workshops.reactive.akka.intro.answers

import akka.actor.{Actor, ActorSystem, Props, Stash}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{MustMatchers, WordSpecLike}

import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success, Try}

class CircuitBreakerAnswers extends TestKit(ActorSystem("test")) with  WordSpecLike with MustMatchers with ImplicitSender{

  import CircuitBreakerExercise._


  "Circuit breaker" should {
    "open circuit when there are more than 3 failures" in {
      val db=new CircuitBreakerMockDatabase
      val props=Props(new CircuitBreakerExercise(db))
      val circuitBreaker=system.actorOf(props)

      circuitBreaker ! Read("m1")
      expectMsgClass(classOf[ReadFailure])

      circuitBreaker ! Read("m2")
      expectMsgClass(classOf[ReadFailure])

      circuitBreaker ! Read("m3")
      expectMsgClass(classOf[ReadFailure])

      circuitBreaker ! Read("m4")
      expectMsgClass(classOf[ReadFailure])

      circuitBreaker ! Read("m5")
      expectMsg(CircuitOpen)
    }

    "schedule circuit close in two seconds" in {
      val db=new CircuitBreakerMockDatabase
      val props=Props(new CircuitBreakerExercise(db))
      val circuitBreaker=system.actorOf(props)

      (1 to 5).map(i=>Read(s"m$i")).foreach(circuitBreaker ! _)

      fishForMessage(FiniteDuration(3,"s"),hint = "wait for circuit open"){
        case CircuitOpen => true
        case _ => false
      }
      println("open")

    }
  }


}

object CircuitBreakerExercise{
  case class Read(input:String)
  sealed trait ReadResult
  case class ReadSuccess(s:String) extends ReadResult
  case class ReadFailure(e:Exception) extends ReadResult
  case object CircuitOpen extends ReadResult


  case object CloseCircuit

}

import CircuitBreakerExercise._
import scala.concurrent.ExecutionContext.Implicits.global

class CircuitBreakerExercise(db:CircuitBreakerDatabase) extends Actor with Stash{
  override def receive: Receive = circuitClose

  val ALLOWED_FAILURES=3
  var failures = 0

  def circuitClose:Receive = {
    case Read(input) =>
      reactOnDb{
        db.read(input).map(ReadSuccess.apply)
      }

      if(failures>ALLOWED_FAILURES) {
        context.become(circuitOpen)
        scheduleCircuitClose()
      }
  }

  def circuitOpen:Receive = {
    case CloseCircuit =>
      context.become(circuitClose)
      unstashAll()
    case _ =>
      sender ! CircuitOpen
      stash()
  }

  def reactOnDb: PartialFunction[Try[ReadResult],Unit] = {
    case Success(i) => sender ! i
    case Failure(e) =>
      sender() ! ReadFailure(e.asInstanceOf[Exception])
      failures=failures+1
  }

  def scheduleCircuitClose() = {
    context.system.scheduler.scheduleOnce(FiniteDuration(2,"s"),self,CloseCircuit)
  }
}

trait CircuitBreakerDatabase{
  def read(input:String): Try[String]
}

class CircuitBreakerMockDatabase extends CircuitBreakerDatabase{
  var readAttempts=0

  override def read(input:String): Try[String] =
    if(readAttempts > 4) Success(s"read : $input") else Failure(new RuntimeException("error"))
}
