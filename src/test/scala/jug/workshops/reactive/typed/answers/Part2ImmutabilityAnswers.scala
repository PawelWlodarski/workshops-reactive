package jug.workshops.reactive.typed.answers

import java.util.concurrent.TimeUnit

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.testkit.typed.scaladsl.{BehaviorTestKit, TestInbox}
import jug.workshops.reactive.typed.answers.ImmutabilityExercise3._
import org.scalatest.{FunSuite, MustMatchers}

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

class Part2ImmutabilityAnswers extends FunSuite with MustMatchers {

  test("Calc should cache calculations") {
    val calc = BehaviorTestKit(ImmutableCalcWithCache.calc())

    val sender = TestInbox[ImmutableCalculationResult]()

    calc.run(ImmutableCalculationMessage(ImmutableAdd(2, 3), sender.ref))
    calc.run(ImmutableCalculationMessage(ImmutableAdd(2, 7), sender.ref))
    calc.run(ImmutableCalculationMessage(ImmutableMultiply(2, 7), sender.ref))



    ImmutableCalcWithCache.cacheForTest.contains(ImmutableAdd(2, 3)) mustBe true
    ImmutableCalcWithCache.cacheForTest.contains(ImmutableAdd(4, 3)) mustBe false
    ImmutableCalcWithCache.cacheForTest.contains(ImmutableAdd(2, 7)) mustBe true
    ImmutableCalcWithCache.cacheForTest.contains(ImmutableMultiply(2, 7)) mustBe true


    sender.expectMessage(ImmutableCalculationResult(5))
    sender.expectMessage(ImmutableCalculationResult(9))
    sender.expectMessage(ImmutableCalculationResult(14))

  }


  //EXERCISE2
  test("Gate should be open for correct credentials") {
    val domainLogic = TestInbox[DomainProtocol2]()
    val caller = TestInbox[LoggingResponse2]()
    val behavior = Gate2.gate(domainLogic.ref)

    val gate = BehaviorTestKit(behavior)

    //when
    gate.run(LogIn2("John", "p4ssword", caller.ref))

    //then
    val UserGranted2(domain) = caller.receiveMessage()

    domain ! DomainCommand12("data")

    domainLogic.expectMessage(DomainCommand12("data"))
  }


  //EXERCISE3
  test("prepare event log"){
    import scala.concurrent.duration._
    import scala.concurrent.ExecutionContext.Implicits.global
    val s1=InMemoryEventLog.saveEvent(ReceivedRequest("1"))
    val s2=InMemoryEventLog.saveEvent(ReceivedRequest("2"))
    val s3=InMemoryEventLog.saveEvent(SendInternalRequest("action3"))

    Await.ready(Future.sequence(Seq(s1,s2,s3)),1 second)

    InMemoryEventLog.events must contain allOf(ReceivedRequest("1"),ReceivedRequest("2"),SendInternalRequest("action3"))
  }

  test("prepare repo"){
    import scala.concurrent.duration._
    val searchingUser1: Future[ImmutabilityExercise3.User] =InMemoryUserRepository.find(1)
    val searchingUser2: Future[ImmutabilityExercise3.User] =InMemoryUserRepository.find(2)

    val user1=Await.result(searchingUser1,1 second)
    user1.email mustBe "someUser@serwer.com"


    the[RuntimeException]  thrownBy{
      Await.result(searchingUser2,1 second)
    } must have message "no user with 2"
  }

  test("prepare API client"){
    import scala.concurrent.duration._
    val sendingRequest=TestApiClient.send(InternalRequest("someone@target.com",777))

    val result=Await.result(sendingRequest,1 second)

    result mustBe InternalResponse(200,"someone@target.com")
  }

}


//EXERCISE1
sealed trait ImmutableCalculation

case class ImmutableAdd(i1: Int, i2: Int) extends ImmutableCalculation

case class ImmutableMultiply(i1: Int, i2: Int) extends ImmutableCalculation

case class ImmutableCalculationMessage(c: ImmutableCalculation, replyTo: ActorRef[ImmutableCalculationResult])

case class ImmutableCalculationResult(r: Int)

object ImmutableCalcWithCache {

  var cacheForTest: Map[ImmutableCalculation, ImmutableCalculationResult] = Map()

  def calc(cache: Map[ImmutableCalculation, ImmutableCalculationResult] = Map()): Behavior[ImmutableCalculationMessage] = {

    cacheForTest = cache

    Behaviors.immutable[ImmutableCalculationMessage] { (_, msg) =>
      val (c,r)=calculateCacheForNextBehavior(cache, msg)
      msg.replyTo ! r
      calc(c)
    }
  }

  private def calculateCacheForNextBehavior(cache: Map[ImmutableCalculation, ImmutableCalculationResult], msg: ImmutableCalculationMessage) =
    cache.get(msg.c) match {
      case Some(v) =>
        (cache,v)
      case _ =>
        val result = calculateResult(msg.c)
        (cache + (msg.c -> result) , result)
    }


  private def calculateResult(calc: ImmutableCalculation): ImmutableCalculationResult = calc match {
    case ImmutableAdd(i1, i2) => ImmutableCalculationResult(i1 + i2)
    case ImmutableMultiply(i1, i2) => ImmutableCalculationResult(i1 * i2)
  }

}


//EXERCISE2

final case class LogIn2(user: String, password: String, replyTo: ActorRef[LoggingResponse2])

sealed trait LoggingResponse2

final case class UserGranted2(application: ActorRef[DomainProtocol2]) extends LoggingResponse2

final case class UserDenied2(message: String) extends LoggingResponse2

sealed trait DomainProtocol2

final case class DomainCommand12(data: String) extends DomainProtocol2

final case class DomainCommand22(data1: Int, data2: Boolean) extends DomainProtocol2

object Gate2 {
  def gate(domain:ActorRef[DomainProtocol2]):Behavior[LogIn2]= Behaviors.immutable{
    case (_, LogIn2(user, password, from)) if user == "John" && password == "p4ssword" =>
      from ! UserGranted2(domain)
      Behaviors.same
    case (_,LogIn2(u, p, from)) =>
      from ! UserDenied2(s"wrong password : [$u,$p]")
      Behaviors.same
  }
}


//EXERCISE 3 - Dependency injection

object ImmutabilityExercise3 {
  class Done private()
  object Done{
    val done=new Done()
  }


  //request
  sealed trait Exercise3Request
  final case class NotifyUser(id:UserId,replyTo:ActorRef[Exercise3Response]) extends Exercise3Request

  case class Exercise3Response(statusCode:Int,from:Email)

  //Event Log
  sealed trait Event
  final case class ReceivedRequest(requestId:String) extends Event
  final case class SendInternalRequest(action:String) extends Event

  trait EventLog{
    def saveEvent(e:Event) : Future[Done]
    def events:Seq[Event]
  }

  object InMemoryEventLog extends EventLog {
    private var storedEvents=List.empty[Event]

    override def saveEvent(e: Event): Future[Done] = {
      storedEvents=e::storedEvents
      TimeUnit.MILLISECONDS.sleep(100)
      Future.successful(Done.done)
    }

    override def events: Seq[Event] = storedEvents
  }
  //User Repo
  type UserId=Long
  type Email=String

  case class User(id:UserId,email:Email)
  trait UserRepository{
    def find(id:UserId):Future[User]
  }

  object InMemoryUserRepository extends UserRepository {
    private val users = Map(
      1L -> User(1L,"someUser@serwer.com")
    )


    override def find(id: UserId): Future[User] =
      users.get(id)
        .map(Future.successful)
        .getOrElse(Future.failed(new RuntimeException(s"no user with $id")))
  }

  //Api Client
  final case class InternalRequest(to:Email,someValue:Int)
  final case class InternalResponse(statusCode:Int,from:Email)
  trait ApiClient{
    def send(r:InternalRequest):Future[InternalResponse]
  }

  object TestApiClient extends ApiClient{
    override def send(r: InternalRequest): Future[InternalResponse] =
      Future.successful(InternalResponse(200,r.to))
  }


  //actor

  def exercise3Behavior(initialValue:Int, eventLog:EventLog, userRepo:UserRepository, api:ApiClient):Behavior[Exercise3Request] = {

    def newExercise3Behavior(value:Int):Behavior[Exercise3Request]  =
      Behaviors.immutable[Exercise3Request]{
        case (ctx,NotifyUser(userId,replyTo)) =>
          implicit val ec=ctx.system.executionContext // no blocking in the for loop
          val preparingRequest=for{
            _ <- eventLog.saveEvent(ReceivedRequest("NotifyUser"))
            user <- userRepo.find(userId)
            r <- api.send(InternalRequest(user.email,initialValue))

          } yield  Exercise3Response(r.statusCode,r.from)

          preparingRequest.onComplete{
            case Success(response) => replyTo ! response
            case Failure(e) => println(s"handling failure $e")
          }(ctx.system.executionContext) // no blocking

          newExercise3Behavior(initialValue+1)
      }

    newExercise3Behavior(initialValue)
  }





}