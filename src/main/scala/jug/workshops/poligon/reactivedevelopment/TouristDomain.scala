package jug.workshops.poligon.reactivedevelopment

import java.util.concurrent.TimeUnit
import java.util.{Currency, Locale}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object TouristDomain {


  def main(args: Array[String]): Unit = {
    val system=ActorSystem("GuideSystem")
    val guideProps=Props[Guidebook]
    val guideBook=system.actorOf(guideProps,"guideBook")

    val touristProps=Props(new Tourist(guideBook))
    val tourist=system.actorOf(touristProps,"tourist1")

    tourist ! Tourist.Start(Locale.getISOCountries)


    TimeUnit.SECONDS.sleep(5)

    system.terminate()
  }

  object Guidebook{
    case class Inquiry(code: String)
  }

  object Tourist{
    case class Guidance(code:String, description:String)
    case class Start(codes: Seq[String])
  }

  import Tourist._
  import Guidebook._

  class Tourist(guidebook: ActorRef) extends Actor {
    override def receive: Receive = {
      case Start(codes) => codes.foreach(guidebook ! Inquiry(_))
      case Guidance(code, description) => println(s"$code: $description")
    }
  }

  class Guidebook extends Actor{
    def describe(locale: Locale) =
    s"""In ${locale.getDisplayCountry},
      ${locale.getDisplayLanguage} is spoken and the currency
      is the ${Currency.getInstance(locale).getDisplayName}"""

    override def receive: Receive = {
      case Inquiry(code) =>
        println(s"Actor ${self.path.name} [CA]responding to inquiry about $code")
        Locale.getAvailableLocales.filter(_.getCountry==code).foreach{locale =>
          sender() ! Guidance(code, describe(locale))
        }

    }
  }

}
