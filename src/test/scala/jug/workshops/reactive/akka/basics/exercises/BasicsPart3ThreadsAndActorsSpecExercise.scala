package jug.workshops.reactive.akka.basics.exercises

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import common.StopSystemAfterAll
import org.scalatest.{MustMatchers, WordSpecLike}

/**
  * Created by pawel on 30.10.16.
  */
class BasicsPart3ThreadsAndActorsSpecExercise extends TestKit(ActorSystem("Threads")) with MustMatchers
    with WordSpecLike with StopSystemAfterAll with ImplicitSender{

  import ExerciseActor._

  //Don't change tests - fix Exercise Actor implementation at the bottom of the file
  "Actor" should {
    "executing logic in different Thread" in {
     val exerciseActor =system.actorOf(Props[ExerciseActor],"exerciseActorTest1")

      exerciseActor ! ThreadId
      val actorThreadId=expectMsgClass(classOf[Long])


      exerciseActor ! ThreadName
      val actorThreadName=expectMsgClass(classOf[String])

      println(s"actor is working in [id : $actorThreadId, name: $actorThreadName]")
      println(s"main thread is [id : $mainThreadId, name: $mainThreadName]")

      actorThreadId must not be equal(mainThreadId)
      actorThreadName must not be equal(mainThreadName)

    }

    "handle list of requests" in {
      val exerciseActor =system.actorOf(Props[ExerciseActor],"exerciseActorTest2")


      exerciseActor ! List(ThreadId,ThreadName)
      val actorThreadId=expectMsgClass(classOf[Long])
      val actorThreadName=expectMsgClass(classOf[String])

      println(s"actor is working in [id : $actorThreadId, name: $actorThreadName]")
      println(s"main thread is [id : $mainThreadId, name: $mainThreadName]")

      actorThreadId must not be equal(mainThreadId)
      actorThreadName must not be equal(mainThreadName)
    }
  }



  def mainThreadId=Thread.currentThread().getId
  def mainThreadName=Thread.currentThread().getName

}

//companion object where all messages are kept
object ExerciseActor{
  case class ThreadId()
  case class ThreadName()
}

class ExerciseActor extends Actor{
  import ExerciseActor._

  override def receive: Actor.Receive = {
    case ThreadId => sender ! ???
    case ThreadName => sender! ???
    case l:List[_] => ???
  }

}