package jug.workshops.poligon.reactivedevelopment.bookscatalog

import scala.compat.Platform

object RareBooksProtocol {
  sealed trait Topic
  case object Africa extends Topic
  case object Asia extends Topic
  case object Gilgamesh extends Topic
  case object Greece extends Topic
  case object Persia extends Topic
  case object Philosophy extends Topic
  case object Royalty extends Topic
  case object Tradition extends Topic
  case object Unknown extends Topic


  /**
    * Viable topics for book requests
    */
  val viableTopics: List[Topic] =
    List(Africa, Asia, Gilgamesh, Greece, Persia, Philosophy, Royalty, Tradition)

  /**
    * Card trait for book cards.
    */
  sealed trait Card {
    def title: String
    def description: String
    def topic: Set[Topic]
  }


  final case class BookCard(
                             isbn: String,
                             author: String,
                             title: String,
                             description: String,
                             dateOfOrigin: String,
                             topic: Set[Topic],
                             publisher: String,
                             language: String,
                             pages: Int)
    extends Card


  trait Msg {
    def dateInMillis: Long
  }

  final case class BookFound(books: List[BookCard], dateInMillis: Long = Platform.currentTime) extends Msg {
    require(books.nonEmpty, "Book(s) required.")
  }

  final case class BookNotFound(reason: String, dateInMillis: Long = Platform.currentTime) extends Msg {
    require(reason.nonEmpty, "Reason is required.")
  }

  final case class Complain(dateInMillis: Long = Platform.currentTime) extends Msg

  final case class Credit(dateInMillis: Long = Platform.currentTime) extends Msg

  final case class FindBookByAuthor(author: String, dateInMillis: Long = Platform.currentTime) extends Msg {
    require(author.nonEmpty, "Author required.")
  }

  final case class FindBookByIsbn(isbn: String, dateInMillis: Long = Platform.currentTime) extends Msg {
    require(isbn.nonEmpty, "Isbn required.")
  }

  final case class FindBookByTopic(topic: Set[Topic], dateInMillis: Long = Platform.currentTime) extends Msg {
    require(topic.nonEmpty, "Topic required.")
  }

  final case class FindBookByTitle(title: String, dateInMillis: Long = Platform.currentTime) extends Msg {
    require(title.nonEmpty, "Title required.")
  }

  final case class GetCustomer(dateInMillis: Long = Platform.currentTime) extends Msg
}
