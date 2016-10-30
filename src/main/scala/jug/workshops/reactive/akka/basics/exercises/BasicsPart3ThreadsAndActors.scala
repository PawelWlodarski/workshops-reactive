package jug.workshops.reactive.akka.basics.exercises

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
    println(s"main thread  : ${Thread.currentThread().getName} : ${Thread.currentThread().getId}")
    demoActor ! "anyMessage"


    //EXERCISE
    // * explain companion object
    // * how to send response to sender
    // * how to send message to self
    //*  how to forward message to retain a sender
    // * potential trap with Object pattern matching
    // * how to run single test in intellij

    system.terminate()
  }


  def waitPrint(msg:String): Unit ={
    TimeUnit.MILLISECONDS.sleep(10)
    println(msg)
  }
}


class DemonstrationActor extends Actor{
  override def receive: Receive = {
    case "TO_SELF" => {println("sending message to self");self ! "MESSAGE_TO_SELF"}
    case "MESSAGE_TO_SELF" => println("received message from SELF")
    case _ => println(s"actors' thread : ${Thread.currentThread().getName} : ${Thread.currentThread().getId}")
  }
}