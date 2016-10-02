package jug.workshops.reactive.patterns.pipesandfilters.answers

import akka.typed.ScalaDSL.Static

/**
  * Created by pawel on 02.10.16.
  */
object PipesTypedAnswer {

  import Domain._

  def main(args: Array[String]): Unit = {

  }

  val orderAcceptanceEndpoint = Static[RawMessage] { msg =>

  }


  object TransportLib {
    def pack(content: String): Array[Byte] = content.toCharArray.map(_.toByte)
    def unpack(bytes: Array[Byte]): String = new String(bytes)
  }

  object OrderParser {
    def parseId(order: String): Option[String] = {
      val regex = "id='([0-9]+).*'".r

      val matched = regex findFirstMatchIn order
      matched.map(_.group(1))
    }
  }

  object Domain {
    type RawMessage = Array[Byte]

    case class ProcessorIncomingOrder(content: Array[Byte])

    object ProcessorIncomingOrder {

      import TransportLib._

      def apply(text: String) = new ProcessorIncomingOrder(pack(text))
    }

  }

}
