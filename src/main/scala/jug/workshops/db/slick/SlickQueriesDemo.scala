package jug.workshops.db.slick

import slick.lifted.ProvenShape

object SlickQueriesDemo extends DatabaseOperations{

  import slick.jdbc.H2Profile.api._   //dependant type import

  case class MyRow(name:String,price:Double)

  def main(args: Array[String]): Unit = {

//    compoundQuery()
//    mappedToObjectQuery()
//    usingSelfReferenceDao
//    useReturning()
//    subqueries()
  }

  class Coffees(tag: Tag) extends Table[(String, Double)](tag, "COFFEES") {
    def name: Rep[String] = column[String]("COF_NAME", O.PrimaryKey)
    def price: Rep[Double] = column[Double]("PRICE")
    def * : ProvenShape[(String, Double)] = (name, price)
  }

  class CoffeesWithId(tag: Tag) extends Table[(Int,String, Double)](tag, "COFFEES") {
    def id:Rep[Int] = column[Int]("ID",O.PrimaryKey,O.AutoInc)
    def name: Rep[String] = column[String]("COF_NAME")
    def price: Rep[Double] = column[Double]("PRICE")
    def * : ProvenShape[(Int,String, Double)] = (id,name, price)
  }

  lazy val coffees = TableQuery[Coffees]


  lazy val coffeesDAO = new TableQuery(new CoffeesWithId(_)){
    type CoffeToInsert = (String,Double)
    type Coffe = (Int,String,Double)

    def createSchema : DBIO[Unit] = this.schema.create

    def insert(cs: CoffeToInsert*):DBIO[Option[Int]] = this ++= cs.toSeq.map(t => (0,t._1,t._2))

    def selectAll() : DBIO[Seq[MyRow]] = this.map(row => (row.name.toUpperCase,row.price).mapTo[MyRow]).result
  }


  private def compoundQuery() = {
    val data = Query(("new_caffe", 56.5))
    val exists: Rep[Boolean] = coffees.filter(_.name === "new_caffe").exists
    val selectForInsert = data.filterNot(_ => exists)
    val insertAction = coffees.forceInsertQuery(selectForInsert)
    val initAction = coffees.schema.create

    println("init action : " + insertAction.statements)

    //exec action

    withDb { db =>
      val actions = DBIO.seq(initAction, insertAction)
      waitForResult(db.run(actions))
      displayResult(db.run(coffees.result)) { case (name, price) => s"name : $name, price : $price" }
    }
  }

  private def mappedToObjectQuery() = {
    val initAction = coffees.schema.create

    val insertAction = coffees ++= Seq(
      ("caffe1", 1.5),
      ("caffe2", 2.5),
      ("caffe3", 3.5),
      ("caffe4", 4.5)
    )

    withDb { db =>
      waitForResult(db.run(DBIO.seq(initAction, insertAction)))
      val selectAction = coffees.map(row => (row.name.toUpperCase, row.price).mapTo[MyRow]).result

      val result: Seq[MyRow] = waitForResult(db.run(selectAction))

      println(s"result : $result")
    }
  }

  private def usingSelfReferenceDao = {
    withDb { db =>
      val actions = DBIO.seq(coffeesDAO.createSchema, coffeesDAO.insert(("dao1", 3.7), ("dao2", 4.7)))
      waitForResult(db.run(actions))
      displayResult(db.run(coffeesDAO.result)) { case (id, name, price) => s"id : $id, name : $name, price : $price" }

    }
  }


  private def useReturning() = {
    withDb { db =>
      val returningId = coffeesDAO returning coffeesDAO.map(_.id)

      val insertAction: DBIO[Seq[Int]] = returningId ++= Seq((0, "dao1", 3.7), (0, "dao2", 4.7))

      waitForResult(db.run(coffeesDAO.createSchema))
      val ids: Seq[Int] = waitForResult(db.run(insertAction))

      println("ids : " + ids)

      // check error
      //      val returningTuple = coffeesDAO returning coffeesDAO.map(row => (row.id,row.name))
      //      val insertAction2 : DBIO[Seq[(Int,String)]]=returningTuple ++= Seq((0,"dao3", 3.7), (0,"dao4", 4.7))
      //      val returnedTuples: Seq[(Int, String)] = waitForResult(db.run(insertAction2))
      //
      //      println("returnedTuples : " + returnedTuples)
    }
  }




  private def subqueries() = {
    val initAction = coffees.schema.create

    val insertAction = coffees ++= Seq(
      ("caffe1", 5.5),
      ("caffe2", 6.5),
      ("caffe3", 3.5),
      ("caffe4", 4.5)
    )

    withDb { db =>
      waitForResult(db.run(DBIO.seq(initAction, insertAction)))

      val mostExpensiveQuery = coffees.sortBy(_.price.desc).map(_.name).take(1)


      val deleteAction = coffees.filter {
        _.name in mostExpensiveQuery
      }.delete

      //      val fromCompiledQueryAction=Compiled(coffees.filter{
      //        _.name in mostExpensiveQuery
      //      }).delete

      waitForResult(db.run(deleteAction))

      val selectAction = coffees.map(row => (row.name.toUpperCase, row.price).mapTo[MyRow]).result

      val result: Seq[MyRow] = waitForResult(db.run(selectAction))

      println(s"result after deletion : $result")
    }
  }

}
