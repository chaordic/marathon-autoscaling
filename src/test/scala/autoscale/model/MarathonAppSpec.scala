package autoscale.model
import autoscale.model.MarathonService.TaskWithStats
import org.scalatest.WordSpec
import org.scalamock.scalatest.MockFactory

/**
 * Created by vitorpaulonavancini on 07/02/17.
 */
class MarathonAppSpec  extends WordSpec with MockFactory {
  val statsPoint1 = Statistics(
    0.3,
    19806.9,
    27507.64,
    1082130432,
    432885760,
    1486752438.07666
  )
  val statsPoint2 = Statistics(
    0.3,
    19807.48,
    27508.2,
    1082130432,
    433037312,
    1486752452.94886
  )





  "A Marathon App" when {
    "configured with max 90% mem AND 90% cpu" should {
      " be overusing for more than 90% of mem usage AND cpu usage" in {
        val appPoint1 = MarathonApp("taskId", List(TaskWithStats("task", TaskStats("executorId",statsPoint1))))
        val appPoint2 = MarathonApp("taskId2", List(TaskWithStats("task2", TaskStats("executorId2",statsPoint2))))
        val usage: Double = MarathonApp.calculateCpuUsage(appPoint1.tasks, appPoint2.tasks)
        println(usage)
        assert(usage != 0.0)
      }
      " not be overusing for more than 90% cpu and less than 90% mem" in {
        val appPoint1 = MarathonApp("taskId", List(TaskWithStats("task", TaskStats("executorId",statsPoint1))))
        val usage: Float = MarathonApp.calculateMemUsage(appPoint1.tasks)
        println(usage)
        assert(usage != 0.0)
      }
    }
  }
}
