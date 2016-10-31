package jug.workshops.reactive.akka.basics.java

import akka.actor.{Props, ActorSystem}

/**
  * Created by pawel on 27.03.16.
  */
object ScalaActorCallingJava {

  def main(args: Array[String]) {
    val system=ActorSystem("scala-calling-java")

    val javaActor=system.actorOf(Props[JavaActor])


    javaActor ! "hello"
    javaActor ! "other message"

    system.terminate()
  }

}
