package jug.workshops.db.slick

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import akka.testkit.TestKit
import com.typesafe.config.ConfigFactory
import common.StopSystemAfterAll
import org.scalatest.{FunSuiteLike, MustMatchers}
import slick.basic.DatabasePublisher
import slick.jdbc.H2Profile.api._

class TestingStreams extends TestKit(ActorSystem("streams")) with FunSuiteLike
  with MustMatchers with StopSystemAfterAll with DatabaseOperations {

  test("slick streaming example"){
    withProductsDB{db =>
      val inserts = DBIO.seq(
        productsTable += ProductExample(0,"Sony1", 7.2),
        productsTable += ProductExample(0,"Toshiba", 10.2),
        productsTable += ProductExample(0,"Sony2", 9.2),
        productsTable += ProductExample(0,"Sony3", 11.2),
        productsTable += ProductExample(0,"Sony4", 5.2)
      )

      waitForResult(db.run(inserts))


      val action = productsTable.filter(_.id > 1).result
      val publisher: DatabasePublisher[ProductExample] =db.stream(action)

      implicit val materializer = ActorMaterializer() // need this for runWith

      Source
        .fromPublisher(publisher)
        .runWith(TestSink.probe)
        .request(1)
        .expectNext(ProductExample(2,"Toshiba", 10.2))  //compilation error
        .request(2)
        .expectNext(ProductExample(3,"Sony2", 9.2),ProductExample(4,"Sony3", 11.2))
        .request(1)
        .expectNext(ProductExample(5,"Sony4", 5.2))
        .expectComplete()
    }
  }


  def withProductsDB(testCode: Database => Any): Unit = {
    val cfg = ConfigFactory.load("db/testslick")
    val db = Database.forConfig("slicktest1", cfg)
    try {
      waitForResult(db.run(productsTable.schema.create))
      testCode(db)
    }
    finally {
      db.close()
    }
  }

  case class ProductExample(id: Int, name: String, price: Double)

  private class ProductsExampleTable(tag: Tag) extends Table[ProductExample](tag, "products") {
    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def name: Rep[String] = column[String]("name")

    def price: Rep[Double] = column[Double]("price")

    override def * = (id,name,price).mapTo[ProductExample]
  }


  private lazy val productsTable = TableQuery[ProductsExampleTable]

}
