package jug.workshops.reactive.akka.a1.exercises

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorSystem, Props}

/**
  * Created by pawel on 26.03.16.
  */
object A1SimpleActor1 {


  def main(args: Array[String]) {
    val system=ActorSystem("workshops")
    val actorProperties = Props[SimpleActor]
    val simpleActorInstance = system.actorOf(actorProperties)


    println("different scala syntax - the same result")
    simpleActorInstance.!("one")  //calling method
    simpleActorInstance !("one")  //without dot
    simpleActorInstance ! "one"  //without simplified

    //exercise : change waitPrint to print and check what will happen
    waitPrint("\nsecond message")
    simpleActorInstance ! "two"

    waitPrint("\nunknown message")
    simpleActorInstance ! "unknown"

    waitPrint("\ndifferent type")
    simpleActorInstance ! 69

    //EXERCISES
//    waitPrint("\nexercise 1 'isEven'")
//    simpleActorInstance ! 40 //40 is even = true
//    simpleActorInstance ! 41 //41 is even = false
//
//
//    waitPrint("\nexercise 2 'calculator'")
//    simpleActorInstance ! (1,2) //result = 3
//    simpleActorInstance ! (4,3) //result = 7
//
//    waitPrint("\nexercise 3 'dedicated calculator'")
//    calcActor ! (1,2) //calc (1+2) = 3
//    calcActor ! (4,3) //calc (4+3) = 7

  }


  class SimpleActor extends Actor{
    override def receive: Receive = {
      case "one" => println("in actor : received one")
      case "two" => println("in actor : received two")
      //EXERCISES
      //case i:Int =>
      case msg => println(s"in actor : received unknown message : [value=$msg, type=${msg.getClass} ]")
    }
  }

  def waitPrint(msg:String): Unit ={
    TimeUnit.MILLISECONDS.sleep(10)
    println(msg)
  }
}
