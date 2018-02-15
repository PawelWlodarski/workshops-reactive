package jug.workshops.poligon.typed.routers

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.{Actor, ActorContext}

import scala.io.StdIn

object TypedRouters1 {

  object ImmutableRoundRobin {
    def roundRobinBehavior[T](numberOfWorkers: Int, worker: Behavior[T]): Behavior[T] =
      Actor.deferred { ctx =>
        val workers = (1 to numberOfWorkers).map { n =>
          ctx.spawn(worker, s"worker-$n")
        }
        activeRoutingBehavior(0, workers.toVector)
      }

    private def activeRoutingBehavior[T](index: Long, workers: Vector[ActorRef[T]]): Behavior[T] =
      Actor.immutable[T] { (ctx, msg) =>
        workers((index % workers.size).toInt) ! msg
        activeRoutingBehavior(index + 1, workers)
      }
  }


  object MutableRoundRobin {
    def roundRobinBehavior[T](numberOfWorkers: Int, worker: Behavior[T]): Behavior[T] =
      Actor.mutable[T](ctx => new MutableRoundRobin(ctx, numberOfWorkers, worker))
  }


  class MutableRoundRobin[T](ctx: ActorContext[T], numberOfWorkers: Int, worker: Behavior[T])
    extends Actor.MutableBehavior[T] {

    private var index = 0L
    private val workers = (1 to numberOfWorkers).map { n =>
      ctx.spawn(worker, s"worker-$n")

    }

    override def onMessage(msg: T): Behavior[T] = {
      workers((index % workers.size).toInt) ! msg
      index += 1
      this
    }
  }


  def main(args: Array[String]): Unit = {
//      immutableRoundRobinExample()
    mutableRoundRobin()
  }


  def mutableRoundRobin(): Unit ={
    val root=Actor.deferred[Nothing]{ctx =>
      val workerPool = ctx.spawn(MutableRoundRobin.roundRobinBehavior(3,Worker.workerBehavior),"workerPool")
      (1 to 20).foreach(n => workerPool ! Worker.Job(n.toString))
      Actor.empty
    }


    val system=ActorSystem[Nothing](root, "RoundRobin")

    try{
      StdIn.readLine()
    }finally{
      system.terminate()
    }

  }


  def immutableRoundRobinExample(): Unit = {
    val root = Actor.deferred[Nothing] { ctx =>
      val workerPool = ctx.spawn(ImmutableRoundRobin.roundRobinBehavior(3, Worker.workerBehavior), "workerPool")
      (1 to 20).foreach(n => workerPool ! Worker.Job(n.toString))
      Actor.empty
    }

    val system=ActorSystem[Nothing](root, "RoundRobin")

    try{
      StdIn.readLine()
    }finally{
      system.terminate()
    }
  }
}
