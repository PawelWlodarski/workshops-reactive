package jug.workshops.reactive.akka.basics.exercises

/**
  * Created by pawel on 26.03.16.
  */
object BasicsPart4PartialFunctions {


  //will this always finish successfully
  val head = (l:List[Int]) => l.head

  //boilerplate
  val partialHead=new PartialFunction[List[Int],Int] {
    override def isDefinedAt(x: List[Int]): Boolean = x.length>0
    override def apply(v1: List[Int]): Int = v1.head
  }

  //synthatic sugar
  val partialHeadPM:PartialFunction[List[Int],Int]={
    case head::tail => head
  }


  def main(args: Array[String]) {
    println("PARTIAL FUNCTIONS DEMO")
    println("    * simple head : "+head(List(1,2,3,4,5)))
//    println(head(List())) // error

    println("\n    * partial head List(1,2,3) : " + partialHead.isDefinedAt(List(1,2,3)))
    println("    * partial head List() : " + partialHead.isDefinedAt(List()))


    println("\n    * partial head Pattern Matching List(1,2,3) : " + partialHeadPM.isDefinedAt(List(1,2,3)))
    println("    * partial head Pattern Matching List() : " + partialHeadPM.isDefinedAt(List()))
    println("    * partial head Pattern Matching List(1,2,3) : " + partialHeadPM(List(1,2,3)))


  }
}