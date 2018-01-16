package jug.workshops.poligon.reactivedevelopment.bookscatalog

import akka.actor.ActorSystem

object RareBooksApp {


  def main(args: Array[String]): Unit = {
    val system=ActorSystem("rareBooksSystem")

    system.actorOf(RareBooks.props,"rareBooks")


  }

}
