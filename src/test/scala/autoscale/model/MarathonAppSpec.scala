package autoscale.model

import autoscale.model.MarathonService.TaskWithStats
import org.scalatest.WordSpec
import org.scalamock.scalatest.MockFactory

/**
  * Created by vitorpaulonavancini on 07/02/17.
  */
class MarathonAppSpec extends WordSpec with MockFactory {
  val statsPoint1 = Statistics(
    0.3,
    19806.9,
    27507.64,
    1082130432,
    432885760,
    1486752438.07666f
  )
  val statsPoint2 = Statistics(
    0.3,
    19807.48,
    27508.2,
    1082130432,
    433037312,
    1486752452.94886f
  )

  val labels = AutoscaleLabels(None, None, None, None, None, None, None, None)

  "A Marathon App" when {
    "when measured for points statsPoint1 and statsPoint2 " should {
      " be using something 25.55 percent of the cpu limit set for this task" in {
        val appPoint1 = MarathonApp("taskId", List(TaskWithStats("task", Option(TaskStats("executorId", statsPoint1)))), labels)
        val appPoint2 = MarathonApp("taskId2", List(TaskWithStats("task2", Option(TaskStats("executorId2", statsPoint2)))), labels)
        val usage: Double = MarathonApp.calculateCpuUsage(appPoint1.tasks, appPoint2.tasks)

        assert(usage == 25.55)
      }
      " be using 40.00 percent of the configured memory for the task for statsPoint1" in {
        val appPoint1 = MarathonApp("taskId", List(TaskWithStats("task", Option(TaskStats("executorId", statsPoint1)))), labels)
        val usage: Double = MarathonApp.calculateMemUsage(appPoint1.tasks)
        //println(usage)
        assert(usage == 40.00)
      }
      " be using 40.02 percent of the configured memory for the task for statsPoint2" in {
        val appPoint1 = MarathonApp("taskId", List(TaskWithStats("task", Option(TaskStats("executorId", statsPoint2)))), labels)
        val usage: Double = MarathonApp.calculateMemUsage(appPoint1.tasks)
        //println(usage)
        assert(usage == 40.02)
      }
    }
  }

  "A Marathon App when asking for its autoscale mode " should {
    "return CPU if BOTH maxCpuPercent and minCpuPercent is set " in {
      val labels = AutoscaleLabels(None, None, None, Option("33"), Option("33"), None, None, None)
      val marathonApp: MarathonApp = MarathonApp("id", List(), labels)

      assert(MarathonApp.getAutoscaleMode(marathonApp) == "CPU")
    }
    "return CPU if ONE OF  maxCpuPercent and minCpuPercent is set " in {
      val labels = AutoscaleLabels(None, None, None, None, Option("33"), None,  None, None)
      val marathonApp: MarathonApp = MarathonApp("id", List(), labels)

      assert(MarathonApp.getAutoscaleMode(marathonApp) == "CPU")

      val labels2 = AutoscaleLabels(None, None, None, Option("33"), None, None,  None, None)
      val marathonApp2: MarathonApp = MarathonApp("id", List(), labels)

      assert(MarathonApp.getAutoscaleMode(marathonApp) == "CPU")
    }
    "return MEM if maxMemPercent and minMemPercent is set " in {
      val labels = AutoscaleLabels(None, Option("33"), Option("33"), None, None, None,  None, None)
      val marathonApp: MarathonApp = MarathonApp("id", List(), labels)

      assert(MarathonApp.getAutoscaleMode(marathonApp) == "MEM")
    }
    "return MEM if nothing is set(mem is the default for now)" in {
      val labels = AutoscaleLabels(None, None, None, None, None, None, None, None)
      val marathonApp: MarathonApp = MarathonApp("id", List(), labels)

      assert(MarathonApp.getAutoscaleMode(marathonApp) == "MEM")
    }
  }
}
