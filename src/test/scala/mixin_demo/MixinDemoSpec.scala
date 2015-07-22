package mixin_demo

import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, FlatSpec}

class MixinDemoSpec extends FlatSpec with Matchers with MockFactory {

  "NetworkInteractive" should "choose" in {
    val interactive = new NetworkInteractive {
      val connection = mock[Connection]
    }

    (interactive.connection.send _).expects("question\nItem1\nItem2")
    (interactive.connection.receive _).expects().returning("Item2")

    interactive.choose("question", Vector("Item1", "Item2")) shouldBe "Item2"
  }
}
