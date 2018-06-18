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



  def withInitiatedDb[A](initialization:DBIO[A]*)(op: Database => Unit): Unit= {
    val cfg = ConfigFactory.load("db/appslick")
    val db = Database.forConfig("slickdbconf1", cfg)

    val initSequence=DBIO.seq(initialization:_*)
    waitForResult(db.run(initSequence.transactionally))

    op(db)
    db.close()
  }

  def waitForResult[A](operation: Future[A], howManySeconds : Int= 1) = {
  import scala.concurrent.duration._
    Await.result(operation, howManySeconds second)
  }


  def displayResult[A](operation:Future[Seq[A]])(displayer : A => String) = {
    val result=waitForResult(operation)
    println("\nDISPLAYING RESULT:")
    result.map(displayer).foreach(println)
  }
}
