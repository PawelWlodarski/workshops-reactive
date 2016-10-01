package common

import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, Suite}

/**
  * Created by pawel on 01.10.16.
  */
trait StopSystemAfterAll extends BeforeAndAfterAll{this : TestKit with Suite =>

  override protected def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }
}
