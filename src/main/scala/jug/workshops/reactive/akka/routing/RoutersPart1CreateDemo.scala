package jug.workshops.reactive.akka.routing

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorSystem, Props}
import akka.routing.{FromConfig, RandomGroup, RoundRobinPool}
import com.typesafe.config.ConfigFactory

object RoutersPart1CreateDemo {

  def main(args: Array[String]): Unit = {
    println("*** CREATING ROUTERS ***")
    //This is how you can pass custom config to an actor system - open this config
    val demoConfig = ConfigFactory.load("routers/routersdemo")
    val system=ActorSystem("routers",demoConfig)

    //Here we are creating workers for group - explain a difference between
    //group and pool
    system.actorOf(Props[GroupSupervisor],"workers")  //reference not needed, only workers declaration


    println("   * CREATING ROUTERS SIMULATION ")
    println("   * TEST DIFFERENT ROUTERS IN MAIN ACTOR RECEIVE METHOD ")
    val demo=system.actorOf(Props[DemoMain],"demoMainActor")

    (1 to 10).foreach(i=>
      demo ! Work(s"value$i")
    )

    TimeUnit.MILLISECONDS.sleep(500)
    system.terminate()
  }


  case class Work(content:String)

  class DemoMain extends Actor{

    //routers and group declaration - from config and from code
    //USING POOL FROM CONFIG
    val router1=context.actorOf(FromConfig.props(Props[RouterWorker]),"routerNameFromConfig1")
    //USING POOL FROM CODE
    val router2 = context.actorOf(RoundRobinPool(3).props(Props[RouterWorker]),"router2")
    //USING GROUP FROM CONFIG
    val group1 = context.actorOf(FromConfig.props(),"group1FromConfig")
    //USING GROUP FROM CODE
    val group2 = context.actorOf(RandomGroup(
      List("/user/workers/groupWorker1","/user/workers/groupWorker2")).props(), "router4"
    )

    //TEST DIFFERENT ROUTERS
    //Routers does not change sender
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
        println("created worker for group : "+child.path)
      }
    }

    override def receive: Receive = {
      case msg =>
    }
  }
}
