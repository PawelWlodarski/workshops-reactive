package jug.workshops.reactive.typed.answers

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.Behaviors.MutableBehavior
import akka.actor.typed.{ActorRef, Behavior}
import akka.testkit.typed.scaladsl.{BehaviorTestKit, TestInbox}
import org.scalatest.{FunSuite, MustMatchers}

class Part1TypedAnswers extends FunSuite with MustMatchers {

  test("Calc should cache calculations"){
    val behavior = new CalcWithCache
    val calc=BehaviorTestKit(behavior)

    val sender=TestInbox[CalculationResult]()

    calc.run(CalculationMessage(Add(2,3),sender.ref))
    calc.run(CalculationMessage(Add(2,7),sender.ref))
    calc.run(CalculationMessage(Multiply(2,7),sender.ref))


    val keyIsInCache:Calculation=>Boolean=CalcWithCache.isInCache(behavior)


    keyIsInCache(Add(2,3)) mustBe true
    keyIsInCache(Add(4,3)) mustBe false
    keyIsInCache(Add(2,7)) mustBe true
    keyIsInCache(Multiply(2,7)) mustBe true


    sender.expectMessage(CalculationResult(5))
    sender.expectMessage(CalculationResult(9))
    sender.expectMessage(CalculationResult(14))

  }


  //EXERCISE2
  test("Gate should be open for correct credentials"){
    val domainLogic=TestInbox[DomainProtocol]()
    val caller=TestInbox[LoggingResponse]()
    val behavior=new Gate(domainLogic.ref)

    val gate=BehaviorTestKit(behavior)

    //when
    gate.run(LogIn("John","p4ssword",caller.ref))

    //then
    val UserGranted(domain)=caller.receiveMessage()

    domain ! DomainCommand1("data")

    domainLogic.expectMessage(DomainCommand1("data"))
  }

  test("Gate should be closed for incorrect credentials"){
    val domainLogic=TestInbox[DomainProtocol]()
    val caller=TestInbox[LoggingResponse]()
    val behavior=new Gate(domainLogic.ref)

    val gate=BehaviorTestKit(behavior)

    //when
    gate.run(LogIn("John","wrong",caller.ref))

    //then
    val response: LoggingResponse = caller.receiveMessage()
    response mustBe a[UserDenied]
  }

}


//EXERCISE1
sealed trait Calculation

case class Add(i1: Int, i2: Int) extends Calculation

case class Multiply(i1: Int, i2: Int) extends Calculation

case class CalculationMessage(c: Calculation, replyTo: ActorRef[CalculationResult])

case class CalculationResult(r: Int)

class CalcWithCache extends MutableBehavior[CalculationMessage] {

  private var cache = Map.empty[Calculation, CalculationResult]

  override def onMessage(msg: CalculationMessage): Behavior[CalculationMessage] = {
    val result = cache.getOrElse(msg.c, updateCache(msg.c))
    msg.replyTo ! result
    Behaviors.same
  }


  private def updateCache(msg: Calculation): CalculationResult = {
    def calculateResult() : CalculationResult = msg match {
      case Add(i1, i2) => CalculationResult(i1 + i2)
      case Multiply(i1, i2) => CalculationResult(i1 * i2)
    }

    val r = calculateResult()
    cache = cache + (msg -> r)
    r
  }
}

object CalcWithCache {
  def isInCache(instance:CalcWithCache)(key:Calculation) = instance.cache.contains(key)


}


//EXERCISE2

final case class LogIn(user:String,password:String,replyTo:ActorRef[LoggingResponse])
sealed trait LoggingResponse
final case class UserGranted(application:ActorRef[DomainProtocol]) extends LoggingResponse
final case class UserDenied(message:String) extends LoggingResponse

sealed trait DomainProtocol
final case class DomainCommand1(data:String) extends DomainProtocol
final case class DomainCommand2(data1:Int,data2:Boolean) extends DomainProtocol

class Gate(domain:ActorRef[DomainProtocol]) extends MutableBehavior[LogIn]{
  override def onMessage(msg: LogIn): Behavior[LogIn] = msg match {
    case LogIn(user,password,from) if user=="John" && password=="p4ssword" =>
      from ! UserGranted(domain)
      Behaviors.same
    case LogIn(u,p,from) =>
      from ! UserDenied(s"wrong password : [$u,$p]")
      Behaviors.same
  }
}