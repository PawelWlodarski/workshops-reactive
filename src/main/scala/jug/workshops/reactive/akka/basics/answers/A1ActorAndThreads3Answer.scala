package jug.workshops.reactive.akka.basics.answers

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, Props, ActorSystem}


/**
  * Created by pawel on 26.03.16.
  */
object A1ActorAndThreads3 {

  def main(args: Array[String]) {
    val system=ActorSystem("Exercise3")
    val demoActor=system.actorOf(Props[DemonstrationActor])

    println("DEMONSTRATION ACTOR THREAD")
    println(s"main thread  : ${Thread.currentThread()}")
    demoActor ! "anyMessage"


    //EXERCISE
    waitPrint("\nEXERCISE - Thread Info")
    import ExerciseActor._
    val exerciseActor=system.actorOf(Props[ExerciseActor])
    exerciseActor ! ThreadId  // should display actor's thread Id
    exerciseActor ! ThreadName // should display actor's thread name
    exerciseActor ! ThreadGroup // should display actor's thread group


    waitPrint("\nADDITIONAL EXERCISE - matching list")
    exerciseActor ! List(ThreadId,ThreadName) // should display both actor's thread Id and thread name

    system.terminate()
  }


  def waitPrint(msg:String): Unit ={
    TimeUnit.MILLISECONDS.sleep(10)
    println(msg)
  }
}


class DemonstrationActor extends Actor{
  override def receive: Receive = {
    case _ => println(s"actors' thread : ${Thread.currentThread()}")
  }
}


object ExerciseActor{
  case class ThreadId()
  case class ThreadName()
  case class ThreadGroup()
}

class ExerciseActor extends Actor{
  import ExerciseActor._

  override def receive: Actor.Receive = {
    case ThreadId => println(s"Exercise Actor Id: ${Thread.currentThread().getId}")
    case ThreadName => println(s"Exercise Actor Name: ${Thread.currentThread().getName}")
    case ThreadGroup => println(s"Exercise Actor Group: ${Thread.currentThread().getThreadGroup}")
    case l:List[_] => l.foreach(e=> self ! e)
  }
}