package autoscale.main

import autoscale.model.{MarathonApp, MarathonServiceTrait, Statistics}
import org.scalamock.scalatest.MockFactory
import org.scalatest.WordSpec
import autoscale.main
/**
  * Created by vitorpaulonavancini on 10/02/17.
  */

class MainSpec  extends WordSpec with MockFactory {
  val marathonApiMock = mock[MarathonServiceTrait]

  val apps1:List[MarathonApp]= List(MarathonApp("apps1", List()))
  val apps2:List[MarathonApp]= List(MarathonApp("apps2", List()))
  val apps3:List[MarathonApp]= List(MarathonApp("apps3", List()))
  val apps4:List[MarathonApp]= List(MarathonApp("apps4", List()))


  "main class append To list" should {
    "append in right order" in {
      var timeline :List[List[MarathonApp]] = List(apps1)
      timeline = Main.appendToTimelineHead(apps2, timeline)
      assert(timeline.head == apps2)
      assert(timeline.length == 2)
      assert(timeline.tail.head == apps1)

      timeline = Main.appendToTimelineHead(apps3, timeline)
      assert(timeline.head == apps3)
      assert(timeline.length == 2)
      assert(timeline.tail.head == apps2)

      timeline = Main.appendToTimelineHead(apps4, timeline)
      assert(timeline.head == apps4)
      assert(timeline.length == 2)
      assert(timeline.tail.head == apps3)
    }
  }
}