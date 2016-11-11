package jug.workshops.reactive.akka.routing.answers

import akka.actor.{Actor, ActorRef, Props}

object Part1RoutersCustomRoundRobinRouterAnswer {


  class CustomRouter(howManyWorkers:Int, workerProps:Props) extends Actor{

    val indexIterator=Cycle.cycleBetween(0,howManyWorkers)

    def generateWorkerName(workerNumber:Int) = {
      val parentName=self.path.name
      s"$parentName:worker:$workerNumber"
    }

    val workers:IndexedSeq[ActorRef] = {
      (1 to howManyWorkers).map(i=> context.actorOf(workerProps,generateWorkerName(i)))
    }


    override def receive: Receive = {
      case msg => workers(indexIterator.next()) forward msg

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
      *   input = RoutedJob("someText")
      * Then
      *   return : "SOMETEXT"
      */
    override def receive: Receive = {
      case RoutedJob(text) => sender ! (text.toUpperCase+self.path.name)
    }
  }


}
