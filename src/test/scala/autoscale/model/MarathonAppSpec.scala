package autoscale.model
import autoscale.model.MarathonService.TaskWithStats
import org.scalatest.WordSpec
import org.scalamock.scalatest.MockFactory

/**
 * Created by vitorpaulonavancini on 07/02/17.
 */
class MarathonAppSpec  extends WordSpec with MockFactory {
  val marathonApiMock = mock[MarathonServiceTrait]
  val statsCpuMemOver90:Statistics = Statistics(
    cpusLimit = 10,
    cpusUserTimeSecs = 5,
    cpusSystemTimeSecs = 4.1f,
    memLimitBytes = 9.1f,
    memRssBytes = 10,
    timestamp = 1000
  )
  val statsCpuOver90:Statistics = Statistics(
    cpusLimit = 10,
    cpusUserTimeSecs = 5,
    cpusSystemTimeSecs = 4.1f,
    memLimitBytes = 10f,
    memRssBytes = 8f,
    timestamp = 1000
  )
//  (marathonApiMock.fetchAppsForScale _).expects().returning(List(MarathonApp("id", List())))

  "A Marathon App" when {
    "configured with max 90% mem AND 90% cpu" should {
      " be overusing for more than 90% of mem usage AND cpu usage" in {
        val appCpuMemOver90 = MarathonApp("taskId", List(TaskWithStats("task", TaskStats("executorId",statsCpuMemOver90))))
        assert(appCpuMemOver90.isOverUsing(0.9f, 0.9f, "AND") == true)
      }
      " not be overusing for more than 90% cpu and less than 90% mem" in {
        val appCpuOver90 = MarathonApp("taskId", List(TaskWithStats("task", TaskStats("executorId",statsCpuOver90))))
        assert(appCpuOver90.isOverUsing(0.9f, 0.9f, "AND") == false)
      }
    }
    "configured with max 90% mem OR 90% cpu" should {
      " be overusing for more than 90% of mem usage and cpu usage" in {
        val appCpuMemOver90 = MarathonApp("taskId", List(TaskWithStats("task", TaskStats("executorId",statsCpuMemOver90))))
        assert(appCpuMemOver90.isOverUsing(0.9f, 0.9f, "OR") == true)
      }
      " be overusing for more than 90% cpu and less than 90% mem" in {
        val appCpuOver90 = MarathonApp("taskId", List(TaskWithStats("task", TaskStats("executorId",statsCpuOver90))))
        assert(appCpuOver90.isOverUsing(0.9f, 0.9f, "OR") == true)
      }
    }
  }
}
