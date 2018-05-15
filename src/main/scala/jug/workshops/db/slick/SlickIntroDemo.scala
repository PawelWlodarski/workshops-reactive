package jug.workshops.db.slick

import com.typesafe.config.ConfigFactory
import slick.lifted.ProvenShape

import scala.concurrent.{Await, Future}

object SlickIntroDemo {

  import slick.jdbc.H2Profile.api._   //dependant type import

  def main(args: Array[String]): Unit = {
    basicQueryTypesAndComposition()
//    compilationErrors()
//    createTable()
//    callDB()
//    composeActions()

  }



  //explain types
  class Coffees(tag: Tag) extends Table[(String, Double)](tag, "COFFEES") {
    def name: Rep[String] = column[String]("COF_NAME", O.PrimaryKey)
    def price: Rep[Double] = column[Double]("PRICE")
    def * : ProvenShape[(String, Double)] = (name, price)
  }

  lazy val coffees = TableQuery[Coffees]

  private def basicQueryTypesAndComposition() = {
    val query: Query[Rep[String], String, Seq] = coffees.map(_.name) //becareful of casting to QueryBase -> broken implicits
    val queryWhere = query.filter(_ === "cafeName") //show implicits

    //beware of casting to DBIO[Seq[String]], no .statements
    //show strange interfered type
    val action = queryWhere.result

    println(action.statements)
    println(query.result.statements)


    val aggregateQuery=coffees.filter(_.price < 10.0).sortBy(_.name).map(_.name)
    println(aggregateQuery.result.statements)
  }

  private def compilationErrors()= {
    //    val q1=coffees.filter(_.name === 7) //compilation error but not on IDEA level!
    //    val q2=coffees.filter(_.name == 7) //also compilation error, expected Rep level
    //    val q3=coffees.filter(_.name < 7) //also compilation error
  }

  private def createTable() = {
    println(coffees.schema.createStatements.mkString(","))
  }


  private def callDB() = {
    val cfg = ConfigFactory.load("db/appslick")
    val db = Database.forConfig("slickdbconf1", cfg)

    //already an action!
    val inserts: DBIO[Option[Int]] = coffees ++= Seq(
      ("coffe1", 5.5),
      ("coffe2", 10),
      ("coffe3", 200)
    )


    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.duration._

    val selectQuery: Query[Rep[String], String, Seq] = coffees.map(_.name.toUpperCase)
    val selectAction = selectQuery.result

    println(s"select action : ${selectAction.statements}")

    val creatingDb: Future[Unit] =db.run(coffees.schema.create)
    lazy val inserting: Future[Option[Int]] = db.run(inserts)


    //composition
    val composedFuture: Future[Seq[String]] =creatingDb.flatMap{ _ =>
      inserting.flatMap {
        case Some(inserted) =>
          println(s"inserted $inserted coffes and now executing select")
          db.run(selectAction)
        case None =>
          // result type is Future[Seq[String]] so if we want use successfull then dedicated ADT
          Future.failed(new RuntimeException("nothing inserted!!!"))
      }
    }

    val selectResult = Await.result(composedFuture, 1 second)
    println(selectResult)
  }

  private def composeActions() = {
    val cfg = ConfigFactory.load("db/appslick")
    val db = Database.forConfig("slickdbconf1", cfg)

    lazy val createSchema: DBIO[Unit] = coffees.schema.create
    lazy val insertCoffees: DBIO[Option[Int]] = coffees ++= Seq(("coffe1", 5.5), ("coffe2", 10), ("coffe3", 200))

    lazy val selectCoffeNames: Option[Int] => DBIO[Seq[String]] = {
      case Some(inserted) =>
        println(s"inserted $inserted coffes and now executing select")
        coffees.map(_.name).result
      case None =>
        DBIO.successful(Seq("MAKABRA!!!"))
    }

    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.duration._

    val composedDBIO: DBIO[Seq[String]] = for {
      _ <- createSchema
      insertingResult: Option[Int] <- insertCoffees
      result <- selectCoffeNames(insertingResult)
    } yield result

    val result: Future[Seq[String]] = db.run(composedDBIO)


    println(Await.result(result, 1 second))
  }

}


