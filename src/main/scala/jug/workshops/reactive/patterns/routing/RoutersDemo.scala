package jug.workshops.reactive.patterns.routing

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorSystem, Props}
import akka.routing.{FromConfig, RandomGroup, RoundRobinGroup, RoundRobinPool}
import com.typesafe.config.ConfigFactory

/**
  * Created by pawel on 23.10.16.
  */
object RoutersDemo {

  def main(args: Array[String]): Unit = {
    //CUSTOM CONFIG
    val demoConfig = ConfigFactory.load("demo")
    val system=ActorSystem("routers",demoConfig)

    //GROUP WORKERS
    system.actorOf(Props[GroupSupervisor],"workers")  //reference not needed, only workers declaration


    val demo=system.actorOf(Props[DemoMain],"demoMainActor")


    (1 to 20).foreach(i=>
      demo ! Work(s"value$i")
    )

    TimeUnit.MILLISECONDS.sleep(500)
    system.terminate()

  }


  case class Work(content:String)

  class DemoMain extends Actor{

    //routers and group declaration - from config and from code
    val router1=context.actorOf(FromConfig.props(Props[RouterWorker]),"routerNameFromConfig1")
    val router2 = context.actorOf(RoundRobinPool(3).props(Props[RouterWorker]),"router2")
    val group1 = context.actorOf(FromConfig.props(),"group1FromConfig")
    val group2 = context.actorOf(RandomGroup(
      List("/user/workers/groupWorker1","/user/workers/groupWorker2")).props(), "router4"
    )

    //TEST DIFFERENT ROUTERS
    override def receive: Receive = {
      case work => router1 ! work
//      case work => router2 ! work
//      case work => group1 ! work
//      case work => group2 ! work
    }
  }

  class RouterWorker extends Actor{
    override def receive: Receive = {
      case Work(value) => println(s"${self.path.toStringWithoutAddress} working on $value in ${Thread.currentThread().getName}")
    }
  }

  //declares worker1 and worker2
  class GroupSupervisor extends Actor {

    @scala.throws[Exception](classOf[Exception])
    override def preStart(): Unit = {
      Seq(1,2).foreach{i=>
        val child=context.actorOf(Props[RouterWorker],s"groupWorker$i")
        println(child.path)
      }
    }


    override def receive: Receive = {
      case msg =>
    }
  }

}
