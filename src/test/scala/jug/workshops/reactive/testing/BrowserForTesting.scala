package jug.workshops.reactive.testing

import akka.actor.{Actor, ActorRef}

object BrowserForTesting {

  //domain
  case class Url(value: String) extends AnyVal

  //not final - you can not extends AnyVal class
  case class Content(value: String) extends AnyVal

  final case class Page(content: String)

  //messages - separate concepts than domain
  sealed trait InternetCall {
    def url: Url
  }

  final case class Call(url: Url) extends InternetCall

  final case class AnonymousCall(url: Url) extends InternetCall

  final case object RequestForHistory

  final case class Response(code: Int, page: Page)

  // not in BrowserMessage trait
  val notFound = (url: Url) => Response(404, Page(s"<h1>Page not found ${url}</h1>"))
  val error = (msg: Any) => Response(500, Page(s"<div>unable to process ${msg}</div>"))

}

import BrowserForTesting._

class BrowserForTesting(internet: ActorRef) extends Actor {

  var browserHistory = List[Url]()

  override def receive: Receive = {
    case call@Call(url) =>
      browserHistory = url :: browserHistory
      internet forward call
    case call: AnonymousCall =>
      internet forward call
    case RequestForHistory =>
      sender ! browserHistory  //should we use domain object here instead of list?
    case msg =>
      sender ! error(msg)

  }
}

//there is an error i internet mock
class InternetMockForTesting extends Actor {

  val pages = Map(
    Url("www.google.pl") -> Page("<input id='query'>"),
    Url("www.trolling.com") -> Page("(╯°□°）╯︵ ┻━┻")
  )

  override def receive: Receive = {
    case call: InternetCall =>
      val url = call.url
      pages.get(url)
        .map(p=>Response(200,p))
        .getOrElse(notFound(url))

    case msg =>
      // why this may be useful even if browser should only pass standard calls
      sender ! error(msg)

  }
}
