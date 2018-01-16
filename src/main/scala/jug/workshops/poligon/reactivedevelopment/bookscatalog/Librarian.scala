package jug.workshops.poligon.reactivedevelopment.bookscatalog

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Stash}

import scala.concurrent.duration.FiniteDuration

object Librarian {

  import Catalog._
  import RareBooksProtocol._

  final case class Done(e: Either[BookNotFound,BookFound], customer:ActorRef)

  private def optToEither[T](v: T, f: T => Option[List[BookCard]]): Either[BookNotFound, BookFound] =
    f(v) match {
      case b: Some[List[BookCard]] => Right(BookFound(b.get))
      case _                       => Left(BookNotFound(s"Book(s) not found based on $v"))
    }

  private def findByIsbn(fb: FindBookByIsbn) =
    optToEither[String](fb.isbn, findBookByIsbn)

  private def findByAuthor(fb: FindBookByAuthor) =
    optToEither[String](fb.author, findBookByAuthor)

  private def findByTitle(fb: FindBookByTitle) =
    optToEither[String](fb.title, findBookByTitle)

  private def findByTopic(fb: FindBookByTopic) =
    optToEither[Set[Topic]](fb.topic, findBookByTopic)

  def props(findBookDuration: FiniteDuration): Props = Props(new Librarian(findBookDuration))
}

class Librarian(findBookDuration: FiniteDuration) extends Actor with ActorLogging with Stash {

  import context.dispatcher
  import Librarian._
  import RareBooksProtocol._

  override def receive: Receive = ready



  private def ready: Receive = {
    case m: Msg => m match {
      case _: Complain =>
        sender ! Credit()
        log.info(s"Credit issued to customer $sender()")
      case f: FindBookByIsbn =>
        research(Done(findByIsbn(f), sender()))
      case f: FindBookByAuthor =>
        research(Done(findByAuthor(f), sender()))
      case f: FindBookByTitle =>
        research(Done(findByTitle(f), sender()))
      case f: FindBookByTopic =>
        research(Done(findByTopic(f), sender()))
    }
  }



  private def busy: Receive = {
    case Done(e, s) =>
      process(e, s)
      unstashAll()
      context.unbecome()
    case _ =>
      stash()
  }

  def process(r: Either[BookNotFound, BookFound], s: ActorRef) = r.fold(
    failure => {s ! failure; log.info(failure.toString)},
    book => s ! book
  )



  def research(done: Done): Unit = {
    context.system.scheduler.scheduleOnce(findBookDuration,self,done)
    context.become(busy)
  }
}
