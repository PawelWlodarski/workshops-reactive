package jug.workshops.reactive.patterns.pipesandfilters.exercises

import akka.actor.{Actor, ActorRef}

/**
  * Created by pawel on 01.10.16.
  */
object PipeStandardExercise {

  val orderText="(encryption)(certificate)<order id='123'>${orderData}</order>"

  val rawOrderBytes=orderText.toCharArray.map(_.toByte)

}


