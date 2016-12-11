package jug.workshops.reactive.akka.routing

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorSystem, Props}
import akka.routing.{FromConfig, GetRoutees, Routees}
import com.typesafe.config.ConfigFactory

object RoutersPart4DynamicRouterDemo {

  def main(args: Array[String]): Unit = {
      val config=ConfigFactory.load("routers/routersdemo")
      val system=ActorSystem("dynamicRouting",config)

      val sender=system.actorOf(Props[Sender],"dynamicDemo")

      sender ! "START"

      TimeUnit.SECONDS.sleep(20)
      system.terminate()

  }

  //change messages-per-resize = 1 to different values
  class Sender extends Actor{

    val workers=context.actorOf(FromConfig.props(Props[DynamicWorker]),"dynamicRouter")

    val tasks=List(10,10,10,10,10,10,10,100,10,10,2000,2000,200,10,10,10,10,10,10,10,2000,5000,5000)

    override def receive: Receive = {
      case "START" =>
        tasks.foreach { time =>
          println(s"sending $time")
          workers ! time
          workers ! GetRoutees
          TimeUnit.MILLISECONDS.sleep(time)  //sequencial message processing in logs is a consequence of blocking operation
        }
      case routes:Routees =>
        println(s"current number of routes" + routes.routees.length)
    }
  }

  class DynamicWorker extends Actor{
    override def receive: Receive = {
      case _ => TimeUnit.MILLISECONDS.sleep(500)
    }
  }

}