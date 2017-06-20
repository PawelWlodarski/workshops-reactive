package jug.workshops.reactive.akka.intro.exercises

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorSystem, Cancellable, Props, Stash}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{MustMatchers, WordSpecLike}

import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success, Try}

class CircuitBreakerExercises extends TestKit(ActorSystem("test")) with  WordSpecLike with MustMatchers with ImplicitSender{

  import CircuitBreakerExercise._


  //First test send 5 messages to circuit breaker. First 4 switches circuit breaker into Open state
  "Circuit breaker" should {
    "open circuit when there are more than 3 failures" in {
      val db=new CircuitBreakerMockDatabase
      val props=Props(new CircuitBreakerExercise(db))
      val circuitBreaker=system.actorOf(props, "exercise1Breaker")

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

    //when circuit breaker becomes open it should schedule close in no more than 2 seconds
    "schedule circuit close in two seconds" in {
      val db=new CircuitBreakerMockDatabase
      val props=Props(new CircuitBreakerExercise(db))
      val circuitBreaker=system.actorOf(props, "exercise2Breaker")

      //Explain parallel collection
      (1 to 5).map(i=>Read(s"m$i")).par.foreach{message =>
        println(s"thread : ${Thread.currentThread().getName} sending $message")
        circuitBreaker ! message
      }

      //explain fish for messages
      fishForMessage(FiniteDuration(3,"s"),hint = "wait for circuit open"){
        case CircuitOpen => true
        case _ => false
      }

      TimeUnit.SECONDS.sleep(2) // for educational purposes
      val probe=TestProbe()
      circuitBreaker.tell(Read("m6"),probe.ref)
      probe.expectMsg(ReadSuccess("read : m6"))

    }

    //use stash to save all messages received by CB when it was open
    "stash all messages when circuit is open and unstash all when it is closed again" in {
      val db=new CircuitBreakerMockDatabase
      val props=Props(new CircuitBreakerExercise(db))
      val circuitBreaker=system.actorOf(props, "exercise3Breaker")

      (1 to 5).map(i=>Read(s"m$i")).foreach{message =>
//        println(s"thread : ${Thread.currentThread().getName} sending $message")
        circuitBreaker ! message
      }

      fishForMessage(FiniteDuration(3,"s"),hint = "wait for circuit open"){
        case CircuitOpen => true
        case _ => false
      }

      expectMsg(ReadSuccess("read : m5"))

    }
  }
}

//All messages are prepared
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
  //we are starting from circuit closed
  override def receive: Receive = circuitClose

  val ALLOWED_FAILURES=3
  var failures = 0

  def circuitClose:Receive = {
    // * react on Read message
    //  * read from db
    //    * if read was successful -> send response to sender
    //    * if read was not successful -> send failure to sender and increase failure counter
    //    * if number of failures is larger than ALLOWED_FAILURES -> open circuit
    //    * EXERCISE 2 - schedule close of circuit in no more than 2 seconds
    ???
  }

  //When circuit is in closed state
  // * react on CloseCircuit by context.become and switching to closed circuit
  //   * EXERCISE 3 - unstash all messages
  // * react on other messages
  //    * by sending CircuitOpen message to sender
  //    * EXERCISE 3 - stash message
  def circuitOpen:Receive = ???

  // EXERCISE2  - schedule circuit close
  def scheduleCircuitClose() = ???
}

//DON'T CHANGE DATABASE. Mock Database fill fail first 4 reads
trait CircuitBreakerDatabase{
  def read(input:String): Try[String]
}

class CircuitBreakerMockDatabase extends CircuitBreakerDatabase{
  var readAttempts=0

  override def read(input:String): Try[String] ={
    readAttempts=readAttempts+1
    if(readAttempts > 4) Success(s"read : $input") else Failure(new RuntimeException("error"))
  }
}
