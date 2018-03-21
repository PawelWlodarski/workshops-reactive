package jug.workshops.reactive.typed


import akka.testkit.typed.scaladsl.{BehaviorTestKit, TestInbox}
import jug.workshops.reactive.akka.typed.Part1MutableTypedActor
import jug.workshops.reactive.akka.typed.Part1MutableTypedActor.{Part1GoodBye, Part1Hello}
import org.scalatest.{FunSuite, MustMatchers}

class Part1SyncTest extends FunSuite with MustMatchers{


  test("Testing mutable state"){
    //given
    val testKit=BehaviorTestKit(new Part1MutableTypedActor.MutableTyped(),"testedMutable")
    val inbox=TestInbox[String]()
    //when

    testKit.run(Part1Hello("test",inbox.ref))
    testKit.run(Part1GoodBye)
    testKit.run(Part1Hello("test2",inbox.ref))

    //then
    inbox.expectMessage("bye to test")
  // import akka.testkit.typed.scaladsl.Effects.Stopped
//    testKit.expectEffect(Stopped("testedMutable"))
  }

}
