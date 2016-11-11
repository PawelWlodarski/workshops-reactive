package jug.workshops.reactive.akka.routing.exercises

import akka.actor.{Actor, ActorRef, Props}

object Part1RoutersCustomRoundRobinRouterExercise {

  /**
    * having : [worker1,worker2,worker3]
    *
    * router will route messages with cycdles
    *
    * msg1 -> worker1
    * msg2 -> worker2
    * msg3 -> worker3
    * msg4 -> worker1
    * msg5 -> worker2
    * msg6 -> worker3
    * msg7 -> worker1
    *...
    *
    * @param howManyWorkers
    * @param workerProps
    */
  class CustomRouter(howManyWorkers:Int, workerProps:Props) extends Actor{

    //change this mechanism to your own if you like or use provided in cycle object
    val indexIterator:Iterator[Int] = ???
    //don't change it - generated worker name is used in test
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
