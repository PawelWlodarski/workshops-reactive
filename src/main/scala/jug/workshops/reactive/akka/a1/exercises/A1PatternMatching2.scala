package jug.workshops.reactive.akka.a1.exercises

/**
  * Created by pawel on 26.03.16.
  */
object A1PatternMatching2 {


  def demonstration1(i:Int):String=i match {
    case 1 => "one"
    case 2 => "two"
    case number => s"other $number"
  }

  def demonstration2(arg:Any):String=arg match{
    case i:Int => "received int"
    case s:String=> "received String"
    case b:BigDecimal=> "received BigDecimal"
  }


  sealed trait User
  case class Customer(email:String,cash:Int) extends User
  case class Admin(login:String) extends User

  def demonstration3(user:User)=user match {
    case Customer(e,c) => s"customer with email ${e} has ${c} credits"
    case Admin(l) => s"Admin with login ${l}"
  }

  //EXERCISES
  def primaryExercise(instruction:(Int,Int,String)) = ???

  //ADDITIONAL
  sealed trait Expression
  case class Number(v:Int) extends Expression
  case class Add(n1:Number,n2:Number) extends Expression
  case class Mult(n1:Number,n2:Number) extends Expression

  def additionalExercise(e:Expression) = ???


  def main(args: Array[String]) {
    println("DEMONSTRATION - NUMBERS")
    println(demonstration1(1))
    println(demonstration1(100))

    println("\nDEMONSTRATION - TYPES")
    println(demonstration2(1))
    println(demonstration2(BigDecimal(1)))


    println("\nDEMONSTRATION - CASE CLASSES")
    println(demonstration3(Admin("a@example.com")))
    println(demonstration3(Customer("c1@example.com",300)))

//    println("\nPRIMARY EXERCISE - BASIC CALC")
//    println(primaryExercise((8,2,"+"))==10)
//    println(primaryExercise((3,1,"+"))==4)
//    println(primaryExercise((8,2,"-"))==6)
//    println(primaryExercise((3,1,"-"))==2)
//    println(primaryExercise((8,2,"*"))==16)
//    println(primaryExercise((3,1,"*"))==3)
//
//
//    println("\nADDITIONAL EXERCISE - EXPRESSION CALC")
//    println(additionalExercise(Number(7))==7)
//    println(additionalExercise(Number(9))==9)
//    println(additionalExercise(Add(Number(7),Number(2)))==9)
//    println(additionalExercise(Add(Number(7),Number(5)))==12)
//    println(additionalExercise(Mult(Number(7),Number(2)))==14)
//    println(additionalExercise(Mult(Number(7),Number(3)))==21)
  }

}
