package jug.workshops.reactive.patterns.routing.exercises

import akka.actor.{Actor, ActorRef, Props}

object CustomRoundRobinRouterExercise {


  class CustomRouter(howManyWorkers:Int, workerProps:Props) extends Actor{

    //change this mechanism to your own if you like
    val indexIterator:Iterator[Int] = ???

    def generateWorkerName(workerNumber:Int) = {
      val parentName=self.path.name
      s"$parentName:worker:$workerNumber"
    }

    //initiate workers pool
    val workers:IndexedSeq[ActorRef] = ???


    //forward to proper worker
    override def receive: Receive = {
      case msg => ???

    }
  }

  object Cycle{
    /**
      *
      * @param start - inclusive
      * @param end - exclusive
      * @return - cycle iterator : for (0,5) it will cycle [0,1,2,3,4,0,1,2,3,4]
      */
    def cycleBetween(start:Int,end:Int) = Iterator.iterate(start)(i=>(i+1)%end)
  }

  case class RoutedJob(toUpper:String)

  class CustomWorker extends Actor{
    /**
      * When
      *   workerName=customRouter:worker:1
      *   input = RoutedJob("someText")
      * Then
      *   return : "SOMETEXTcustomRouter:worker:1"
      */
    override def receive: Receive = {
      case RoutedJob(text) => sender ! (text.toUpperCase+self.path.name)
    }
  }


}
