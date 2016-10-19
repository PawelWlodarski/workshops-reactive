package jug.workshops.reactive.akka.basics.answers

import org.scalatest.{MustMatchers, WordSpec}

class BasicsPart2PatternMatchingSpecAnswer extends WordSpec with MustMatchers {

  import PatternMatchingExercise._

  "Pattern Matching Exercise" should {
    "properly match message (Int,Int,String)" in {

      primaryExercise((8, 2, "+")) mustBe 10
      primaryExercise((3, 1, "+")) mustBe 4
      primaryExercise((8, 2, "-")) mustBe 6
      primaryExercise((3, 1, "-")) mustBe 2
      primaryExercise((8, 2, "*")) mustBe 16
      primaryExercise((3, 1, "*")) mustBe 3
    }


    "Properly match case classes" in {
      additionalExercise(Number(7)) mustBe 7
      additionalExercise(Number(9)) mustBe 9
      additionalExercise(Add(Number(7), Number(2))) mustBe 9
      additionalExercise(Add(Number(7), Number(5))) mustBe 12
      additionalExercise(Mult(Number(7), Number(2))) mustBe 14
      additionalExercise(Mult(Number(7), Number(3))) mustBe 21
    }

  }

}


object PatternMatchingExercise {
  def primaryExercise(instruction: (Int, Int, String)) = instruction match {
    case (i1, i2, "+") => i1 + i2
    case (i1, i2, "-") => i1 - i2
    case (i1, i2, "*") => i1 * i2
  }

  sealed trait Expression

  case class Number(v: Int) extends Expression

  case class Add(n1: Number, n2: Number) extends Expression

  case class Mult(n1: Number, n2: Number) extends Expression

  def additionalExercise(e: Expression) = e match {
    case Number(v) => v
    case Add(Number(v1), Number(v2)) => v1 + v2
    case Mult(Number(v1), Number(v2)) => v1 * v2
  }

}
