package jug.workshops.db.slick.exercises

import com.typesafe.config.ConfigFactory
import jug.workshops.db.slick.DatabaseOperations
import org.scalatest.{FunSuite, MustMatchers}
import slick.jdbc.H2Profile.api._
import slick.lifted.{MappedProjection, ProvenShape}

class SlickQueriesExercises extends FunSuite with MustMatchers with DatabaseOperations {

  test("return id of inserted row") {
    withProductsDB { db =>

      val initialState: DBIO[Option[Int]] = ProductsDao.dao.getMaxId

      waitForResult(db.run(initialState)) mustBe None

      val action = ProductsDao.dao.add("newProduct", 7.2)
      val id = waitForResult(db.run(action))
      id mustBe 1

      waitForResult(db.run(ProductsDao.dao.getMaxId)) mustBe Some(1)

      val action2 = ProductsDao.dao.add("newProduct2", 7.5)
      val id2 = waitForResult(db.run(action2))
      id2 mustBe 2
    }

  }


  test("add product only if it doesn't exist") {
    withProductsDB { db =>
      val inserts = DBIO.seq(
        ProductsDao.dao.add("newProduct", 7.2),
        ProductsDao.dao.add("newProduct2", 8.2),
        ProductsDao.dao.add("newProduct3", 9.2)
      )

      waitForResult(db.run(inserts))

      val insert1 = ProductsDao.dao.addIfNotExist("newProduct2", 10.2)
      val insert2 = ProductsDao.dao.addIfNotExist("newProduct4", 20.2)

      waitForResult(db.run(insert1)) mustBe 0
      waitForResult(db.run(insert2)) mustBe 1


      val rows: Seq[(Int, String, Double)] = waitForResult(db.run(ProductsDao.dao.result))

      rows mustBe Vector(
        (1, "newProduct", 7.2), (2, "newProduct2", 8.2), (3, "newProduct3", 9.2), (4, "newProduct4", 20.2)
      )

    }
  }


  test("select 2 most expensive products") {
    case class ProductIdProjection(id: Int, name: String)



    withProductsDB { db =>
      val inserts = DBIO.seq(
        ProductsDao.dao.add("Sony1", 7.2),
        ProductsDao.dao.add("Toshiba", 10.2),
        ProductsDao.dao.add("Sony2", 9.2),
        ProductsDao.dao.add("Sony3", 11.2),
        ProductsDao.dao.add("Sony4", 5.2)
      )


      waitForResult(db.run(inserts))

      //exercise
      val query: Query[MappedProjection[ProductIdProjection, (Int, String)], ProductIdProjection, Seq] = ???

      val action: DBIO[Seq[ProductIdProjection]] = query.result

      val result = waitForResult(db.run(action))

      result mustBe Vector(ProductIdProjection(4,"Sony3"), ProductIdProjection(3,"Sony2"))

    }
  }


  def withProductsDB(testCode: Database => Any): Unit = {
    val cfg = ConfigFactory.load("db/testslick")
    val db = Database.forConfig("slicktest1", cfg)
    try {
      waitForResult(db.run(ProductsDao.dao.schema.create))
      testCode(db)
    }
    finally {
      db.close()
    }
  }
}

object ProductsDao {
  type ProductRow = (Int, String, Double)

  lazy val dao = new TableQuery(new ProductsDao(_)) {
    //exercise
    def add(name: String, price: Double): DBIO[Int] = ???
    //exercise
    def getMaxId: DBIO[Option[Int]] = ???
    //exercise
    def addIfNotExist(name: String, price: Double): DBIO[Int] = ???

  }

}

class ProductsDao(tag: Tag) extends Table[ProductsDao.ProductRow](tag, "products") {
  def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

  def name: Rep[String] = column[String]("name")

  def price: Rep[Double] = column[Double]("price")

  override def * : ProvenShape[(Int, String, Double)] = (id, name, price)
}

case class Product(id: Int, name: String, price: Double)