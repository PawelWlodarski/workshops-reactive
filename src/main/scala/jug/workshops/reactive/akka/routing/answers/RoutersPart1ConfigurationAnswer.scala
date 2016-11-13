package jug.workshops.reactive.akka.routing.answers

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.routing.FromConfig
import com.typesafe.config.ConfigFactory
import jug.workshops.reactive.patterns.Displayer

/**
  * Created by pawel on 23.10.16.
  */
object RoutersPart1ConfigurationAnswer {

  def main(args: Array[String]): Unit = {
    val demoConfig = ConfigFactory.load("routers/answer")
    val system=ActorSystem("routersExercise",demoConfig)

    val exerciseActor=system.actorOf(Props[MathEndPoint],"exerciseActor")
    val displayer=system.actorOf(Props[Displayer],"displayer")


    exerciseActor.tell(AddOperation(1 to 5),displayer)

    //group workers
    system.actorOf(Props[ExerciseGroup],"exerciseGroup")

    exerciseActor.tell(PowOperation(2,3),displayer)
    exerciseActor.tell(PowOperation(2,4),displayer)
    exerciseActor.tell(PowOperation(2,5),displayer)

    TimeUnit.MILLISECONDS.sleep(1100)
    system.terminate()
  }


  sealed trait MathOperation
  case class AddOperation(numbers:Range) extends MathOperation
  case class PowOperation(number:Int, power:Int) extends MathOperation

  case class MathResponse(operation:MathOperation,response:Int)

  class MathEndPoint extends Actor{

    val router=context.actorOf(FromConfig.props(Props[AddWorker]),"router1")
    val group=context.actorOf(FromConfig.props(Props[PowerWorker]),"group1")

    override def receive: Receive = {
      case add : AddOperation => router forward add
      case pow : PowOperation => group forward  pow
    }
  }


  class AddWorker extends Actor{
    override def receive: Receive = {
      case add @ AddOperation(numbers) =>
        TimeUnit.MILLISECONDS.sleep(500)
        val result=numbers.sum
        sender ! MathResponse(add,result)
    }
  }

  class PowerWorker extends Actor with ActorLogging{


    @scala.throws[Exception](classOf[Exception])
    override def preStart(): Unit = log.info(s"starting PowerWorker ${self.path.toStringWithoutAddress}")

    override def receive: Receive = {
      case pow @ PowOperation(number,power) =>
        TimeUnit.MILLISECONDS.sleep(500)
        val result=Math.pow(number,power)
        sender ! MathResponse(pow,result.toInt)
    }
  }

  class ExerciseGroup extends Actor{

    @scala.throws[Exception](classOf[Exception])
    override def preStart(): Unit = {
      (1 to 3).foreach{i=> context.actorOf(Props[PowerWorker],s"Worker$i")}
    }


    override def receive: Receive = {
      case msg => throw new RuntimeException(s"Exercise group should not receive $msg")
    }
  }

}
