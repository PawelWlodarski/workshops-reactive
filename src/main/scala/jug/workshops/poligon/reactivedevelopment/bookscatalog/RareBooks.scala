package jug.workshops.poligon.reactivedevelopment.bookscatalog

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Stash}

import scala.concurrent.duration.{Duration, FiniteDuration, MILLISECONDS => Millis}

object RareBooks {

  case object Close

  case object Open

  case object Report

  def props: Props = Props(new RareBooks)
}

class RareBooks extends Actor with ActorLogging with Stash {

  import RareBooks._
  import RareBooksProtocol._

  context.system.scheduler.scheduleOnce(openDuration, self, Close)

  override def receive: Receive = open


  private def open: Receive = {
    case m: Msg =>
      librarian forward m
      requestsToday += 1
    case Close =>
      context.system.scheduler.scheduleOnce(closeDuration,self,Open)
      log.info("Closing down for the day")
      context.become(closed)
  }

  private def closed : Receive={
    case Report =>
      totalRequests += requestsToday
      log.info(s"$requestsToday requests processed today. Total requests processed = $totalRequests")
      requestsToday = 0
    case Open =>
      context.system.scheduler.scheduleOnce(openDuration, self, Close)
      unstashAll()
      log.info("Time to open up!")
      context.become(open)
    case _ =>
      stash()
  }

  private val librarian = createLibrarian()

  var requestsToday: Int = 0
  var totalRequests: Int = 0

  private val openDuration: FiniteDuration =
    Duration(context.system.settings.config.getDuration("rare-books.open-duration", Millis), Millis)

  private val closeDuration: FiniteDuration =
    Duration(context.system.settings.config.getDuration("rare-books.close-duration", Millis), Millis)

  private val findBookDuration: FiniteDuration =
    Duration(context.system.settings.config.getDuration("rare-books.librarian.find-book-duration", Millis), Millis)

  protected def createLibrarian(): ActorRef =
    context.actorOf(Librarian.props(findBookDuration))
}
