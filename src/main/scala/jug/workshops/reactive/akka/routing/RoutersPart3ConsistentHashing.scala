package jug.workshops.reactive.akka.routing

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorSystem, Props}
import akka.routing.ConsistentHashingRouter.{ConsistentHashMapping, ConsistentHashable}
import akka.routing.{Broadcast, ConsistentHashingPool}

object RoutersPart3ConsistentHashing {

  def main(args: Array[String]): Unit = {
    val system=ActorSystem("HashBasedRouting")


    val router=system.actorOf(ConsistentHashingPool(3,virtualNodesFactor = 10).props(Props[HashingWorker]),"pureHashRouter")

//HANDS ON - router with configured hashing strategy
//    def routingStrategy:ConsistentHashMapping={
//      case NormalMessage(id,_) => id
//    }
//    val router=system.actorOf(ConsistentHashingPool(3,virtualNodesFactor = 10,hashMapping = routingStrategy)
//      .props(Props[HashingWorker]),"pureHashRouter")

    router ! HashedMessage(1,"Msg[1:1]")
    router ! HashedMessage(2,"Msg[2:1]")
    router ! HashedMessage(3,"Msg[3:1]")
    router ! HashedMessage(1,"Msg[1:2]")
    router ! HashedMessage(2,"Msg[2:2]")
    router ! HashedMessage(3,"Msg[3:2]")
    router ! HashedMessage(1,"Msg[1:3]")
    router ! HashedMessage(2,"Msg[2:3]")
    router ! HashedMessage(4,"Msg[4:1]")
    router ! HashedMessage(5,"Msg[5:1]")
    router ! HashedMessage(5,"Msg[5:2]")
    router ! HashedMessage(4,"Msg[4:2]")
    router ! Broadcast("DISPLAY")



    TimeUnit.MILLISECONDS.sleep(500)
    system.terminate()

  }

  case class HashedMessage(id:Int,content:String) extends ConsistentHashable{
    override def consistentHashKey: Any = id
  }

  case class NormalMessage(id:Int,content:String)

  class HashingWorker extends Actor{

    var ids=Set[Int]()

    override def receive: Receive = {
      case msg : HashedMessage =>
        println(s"${self.path.toStringWithoutAddress} received Hashed Message with id : ${msg.id}: ${msg.content}")
        ids = ids + msg.id
      case msg : NormalMessage =>
        println(s"${self.path.toStringWithoutAddress} received Normal Message with id : ${msg.id} : ${msg.content}")
        ids = ids + msg.id
      case "DISPLAY" => println(s"${self.path.toStringWithoutAddress} has ids : ${ids}")
    }
  }

}
