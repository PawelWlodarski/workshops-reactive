package jug.workshops.reactive.akka.basics.exercises

/**
  * Created by pawel on 26.03.16.
  */
object BasicsPart2PatternMatching {


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


  sealed trait User // Explain 'sealed'
  case class Customer(email:String,cash:Int) extends User
  case class Admin(login:String) extends User

  def demonstration3(user:User)=user match {
    case Customer(e,c) => s"customer with email ${e} has ${c} credits"
    case Admin(l) => s"Admin with login ${l}"
  }

  def main(args: Array[String]) {
    println("*** PATTERN MATCHING ON VALUES***")
    println("  ** DEMONSTRATION - PATTERN MATCHING NUMBERS")
    println("  **     "+demonstration1(1))
    println("  **     "+demonstration1(100))

    println("\n*** DEMONSTRATION - PATTERN MATCHING ON TYPES")
    println("  **     "+demonstration2(1))
    println("  **     "+demonstration2(BigDecimal(1)))


    println("\n*** DEMONSTRATION - PATTERN MATCHING ON CASE CLASSES")  //beware of Object!
    println("  **     "+demonstration3(Admin("a@example.com")))
    println("  **     "+demonstration3(Customer("c1@example.com", 300)))
  }

}
