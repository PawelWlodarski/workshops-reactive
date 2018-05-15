package jug.workshops.db.slick.answers

import com.typesafe.config.ConfigFactory
import org.scalatest.{FunSuite, MustMatchers}
import slick.jdbc.H2Profile.api._

import scala.concurrent.{Await, Future}

class SlickIntroAnswers extends FunSuite with MustMatchers{
  import UsersTable._


  test("create, insert and select"){
//      import scala.concurrent.ExecutionContext.Implicits.global  //not needed for andThen
      import scala.concurrent.duration._


      val db=createDB()

      val insertAction : DBIO[Seq[String]] =
          createTable() andThen
          insertRows() andThen
          studentsNames(selectStudentsFromLodz()).result


      val selectingNames: Future[Seq[String]] =db.run(insertAction)

      val selectedNames=Await.result(selectingNames,1 second)

      selectedNames mustBe Vector("Student1", "Student3", "Student4", "Student6")

      db.close()
  }

  test("grade average"){
    import scala.concurrent.duration._


    val db=createDB()

    val gradeAverageFromLodz : DBIO[Option[Double]] =
      createTable() andThen
        insertRows() andThen
      gradeAverageFromCity("Lodz").result


    val lodzResult=Await.result(db.run(gradeAverageFromLodz),1 second)

    lazy val gradeAverageFromRadom : DBIO[Option[Double]] = gradeAverageFromCity("Radom").result
    val radomResult=Await.result(db.run(gradeAverageFromRadom),1 second)

    lazy val noneAverage : DBIO[Option[Double]] = gradeAverageFromCity("Olaboga").result
    val olabogaResult=Await.result(db.run(noneAverage),1 second)

    lodzResult mustBe Some(4.75)
    radomResult mustBe Some(3.5)
    olabogaResult mustBe None

    db.close()

  }




  def createTable() : DBIO[Unit] = students.schema.create

  def insertRows() : DBIO[Option[Int]] = students ++= Seq(
    (0,"Student1","Lodz",4.5),
    (0,"Student2","Radom",4.0),
    (0,"Student3","Lodz",5.0),
    (0,"Student4","Lodz",5.0),
    (0,"Student5","Radom",3.0),
    (0,"Student6","Lodz",4.5)
  )

  def selectStudentsFromLodz() : Query[UsersTable, User, Seq] = students.filter(_.city === "Lodz")

  def studentsNames(filteredStudents: Query[UsersTable, User, Seq]): Query[Rep[String], String, Seq] = filteredStudents.map(_.name)


  def gradeAverageFromCity(city:String): Rep[Option[Double]] = students.filter(_.city===city).map(_.grade).avg


  def createDB() : Database={
    val cfg=ConfigFactory.load("db/testslick")
    Database.forConfig("slicktest1",cfg)
  }

}

object UsersTable{
  type User=(Int,String,String,Double)

  val students = TableQuery[UsersTable]
}

import jug.workshops.db.slick.answers.UsersTable.User
class UsersTable(tag:Tag) extends Table[User](tag,"users"){
  def id : Rep[Int]=column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def name: Rep[String] = column[String]("NAME")
  def city: Rep[String] = column[String]("CITY")
  def grade: Rep[Double] = column[Double]("GRADE")

  override def * = (id,name,city,grade)
}
