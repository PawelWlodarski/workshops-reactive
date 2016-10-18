package jug.workshops.reactive.akka.basics.answers

/**
  * Created by pawel on 26.03.16.
  */
object A1PartialFunctions4 {


  val head = (l:List[Int]) => l.head

  val partialHead=new PartialFunction[List[Int],Int] {
    override def isDefinedAt(x: List[Int]): Boolean = x.length>0
    override def apply(v1: List[Int]): Int = v1.head
  }

  val partialHeadPM:PartialFunction[List[Int],Int]={
    case head::tail => head
  }

  def main(args: Array[String]) {
    println("PARTIAL FUNCTIONS DEMO")
    println("simple head"+head(List(1,2,3,4,5)))
//    println(head(List()))

    println("\npartial head List(1,2,3) : " + partialHead.isDefinedAt(List(1,2,3)))
    println("partial head List() : " + partialHead.isDefinedAt(List()))


    println("\npartial head Pattern Matching List(1,2,3) : " + partialHeadPM.isDefinedAt(List(1,2,3)))
    println("partial head Pattern Matching List() : " + partialHeadPM.isDefinedAt(List()))
    println("partial head Pattern Matching List(1,2,3) : " + partialHeadPM(List(1,2,3)))

    //EXERCISE
    println("\nEXERCISE PARTIAL FUNCTIONS CALCULATOR")
    lazy val add:PartialFunction[(Int,Int,String),Int] = {
      case (a,b,"+") => a+b
    }

    lazy val mult:PartialFunction[(Int,Int,String),Int] = {
      case (a,b,"*") => a*b
    }


    lazy val calc=add orElse mult
    println(calc(1,2,"+")==3)
    println(calc(6,2,"*")==12)
    println(calc((6,3,"*"))==18)

    println("\nADDITIONAL EXERCISE - STATE ENCAPSULATION")
    import ObjectWithState._
    val encapsulatedState=new ObjectWithState()
    encapsulatedState.receive(Add(2))
    encapsulatedState.receive(Add(3))
    encapsulatedState.receive(PrintState)
    encapsulatedState.receive(Mult(6))
    encapsulatedState.receive(PrintState)
  }

}

object ObjectWithState{
  case class Add(v:Int)
  case class Mult(v:Int)
  case class PrintState()
}

class ObjectWithState{
  import ObjectWithState._
  type Receive = PartialFunction[Any, Unit]

  private var state:Int=0

  val receive:Receive={
    case Add(n) => state = state + n
    case Mult(n) => state = state * n
    case PrintState => println(s"state in object : $state")
  }
}


/*
object Actor {
  /**
   * Type alias representing a Receive-expression for Akka Actors.
   */
  //#receive
  type Receive = PartialFunction[Any, Unit]
 */