package jug.workshops.reactive.akka.a1.exercises

import java.util.Date
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

/**
  * Created by pawel on 26.03.16.
  */
object BasicsPart1SimpleActor1 {


  def main(args: Array[String]) {
    //what is actorSystem and why we use Props to create actors in factory method instead of simple "new"
    val system=ActorSystem("workshops")
    val actorProperties = Props[SimpleActor]
    //what is actor Ref??
    val simpleActorInstance: ActorRef = system.actorOf(actorProperties)

    //HANDS ON :uncomment first match in 'SimpleActor'
    println("different scala syntax - the same result")
    simpleActorInstance.!("one")  //calling method
    simpleActorInstance !("one")  //without dot
    simpleActorInstance ! "one"  //without simplified

    //HANDS ON : change waitPrint to print and check what will happen
    //HANDS ON :uncomment second message
    waitPrint("\nsecond message")
    simpleActorInstance ! "two"

    //HANDS ON : uncomment default
    waitPrint("\nunknown message")
    simpleActorInstance ! "unknown"

    //HANDS ON : ad hoc implementation for different types
    waitPrint("\ndifferent type")
    simpleActorInstance ! 69
    simpleActorInstance ! new Date()
    simpleActorInstance ! List(1,2,3,4,5)
    simpleActorInstance ! (1,2)  //explain tuple for exercises


    system.terminate()
  }


  class SimpleActor extends Actor{
    override def receive: Receive = {
//      case "one" => println("in actor : received one")
//      case "two" =>
//        println("sending message two to sender")
//        sender ! "in actor : received two"
//      case msg => println(s"in actor : received unknown message : [value=$msg, type=${msg.getClass} ]")
      case _ => println("this catches all messages - uncomment specific cases")
    }
  }

  def waitPrint(msg:String): Unit ={
    TimeUnit.MILLISECONDS.sleep(10)
    println(msg)
  }
}
