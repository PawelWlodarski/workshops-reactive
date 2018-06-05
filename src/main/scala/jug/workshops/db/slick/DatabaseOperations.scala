package jug.workshops.db.slick

import com.typesafe.config.ConfigFactory

import scala.concurrent.{Await, Future}

trait DatabaseOperations {

  import slick.jdbc.H2Profile.api._

  def withDb[A](op: Database => Unit): Unit= {
    val cfg = ConfigFactory.load("db/appslick")
    val db = Database.forConfig("slickdbconf1", cfg)
    op(db)
    db.close()
  }


  def waitForResult[A](operation: Future[A]) = {
    import scala.concurrent.duration._
    Await.result(operation, 1 second)
  }


  def displayResult[A](operation:Future[Seq[A]])(displayer : A => String) = {
    val result=waitForResult(operation)
    println("\nDISPLAYING RESULT:")
    result.map(displayer).foreach(println)
  }
}
