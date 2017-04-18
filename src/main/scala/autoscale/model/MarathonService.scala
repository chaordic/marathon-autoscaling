package autoscale.model

import autoscale.model.MarathonService.TaskWithStats
import org.json4s.FieldSerializer._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization

import scalaj.http.{Http, HttpResponse}

/**
  * Created by vitorpaulonavancini on 03/02/17.
  *
  * Represents the marathon rest api
  */

trait MarathonServiceTrait {
  def fetchAppsForScale(): Seq[MarathonApp]

  def getAppTasks(appId: String): Seq[TaskWithStats]

  def getTasksStats(task: Task): Option[TaskStats]

  def scaleApp(appId: String, scalePolicy: Int)
}

case class MarathonResponse(id: String, labels: AutoscaleLabels)

case class AutoscaleLabels(autoscale: Option[String],
                           maxMemoryPercent: Option[String],
                           minMemoryPercent: Option[String],
                           maxCpuPercent: Option[String],
                           minCpuPercent: Option[String],
                           maxInstanceCount: Option[String],
                           minInstanceCount: Option[String],
                           scaleFactor: Option[String]
                          )

object MarathonService extends MarathonServiceTrait {
  def scaleApp(appId: String, scalePolicy: Int): Unit = println("scaling!");

  val AUTO_SCALE_LABEL: String = "autoscale"
  val MARATHON_HOST = ""

  def fetchAppsForScale(): List[MarathonApp] = {
    implicit val formats = DefaultFormats
    implicit val marathonSource = this

    val response: HttpResponse[String] = Http(s"$MARATHON_HOST/v2/apps").asString
    val responseJson: JValue = parse(response.body)
    val appsJson: JValue = responseJson \ "apps"
    val marathonApps: List[MarathonResponse] = Serialization.read[List[MarathonResponse]](Serialization.write(appsJson))

    marathonApps.filter(_.labels.autoscale.nonEmpty).map((marathonApp: MarathonResponse) => {
      MarathonApp(marathonApp.id, getAppTasks(marathonApp.id), marathonApp.labels)
    })
  }

  def getTasksStats(task: Task): Option[TaskStats] = {
    implicit val marathonSource = this
    implicit val formats = DefaultFormats +
      TaskStats.renameTaskStats +
      Statistics.taskStatisticsRename

    val host = task.host
    val response: HttpResponse[String] = Http(s"http://$host:5051/monitor/statistics.json").header("Cache-Control", "no-cache").asString
    val allStats: Seq[TaskStats] = Serialization.read[Seq[TaskStats]](response.body)

    allStats.filter(_.executorId == task.id).headOption
  }

  case class TaskWithStats(id: String, stats: Option[TaskStats])

  case class AppTasks(tasks: Seq[Task])

  def getAppTasks(appId: String): Seq[TaskWithStats] = {
    implicit val formats = DefaultFormats
    implicit val marathonSource = this

    val response: HttpResponse[String] = Http(s"${MarathonService.MARATHON_HOST}/v2/apps/$appId").asString
    val responseJson: JValue = parse(response.body)
    val appJson: JValue = responseJson \ "app"
    val appTasks: AppTasks = Serialization.read[AppTasks](Serialization.write(appJson))
    appTasks.tasks.map((task: Task) => {
      TaskWithStats(task.id, getTasksStats(task))
    })
  }

  case class ScaleRequest(instances: Int)

  def scaleApp(app: MarathonApp, scalePolicy: Int, taskMaxCount: Int, taskMinCount: Int) = {
    implicit val formats = DefaultFormats

    val newInstanceCount = getNewInstanceCount(app, scalePolicy, taskMaxCount, taskMinCount)
    if (newInstanceCount != app.tasks.length) {
      val headers = {
        "Content-type" -> "Application/json"
      }
      val requestData = Serialization.write(ScaleRequest(newInstanceCount))
      println(Http(s"$MARATHON_HOST/v2/apps/${app.id}").headers(headers).put(requestData).execute().body)
    } else {
      println("same count, not scaling")
    }
  }

  def getNewInstanceCount(app: MarathonApp, scalePolicy: Int, taskMaxCount: Int, taskMinCount: Int): Int = {
    val newInstanceCount: Int = app.tasks.length + scalePolicy
    newInstanceCount match {
      case count if count >= taskMaxCount => {
        println(s" number of tasks reached max of $taskMaxCount")
        taskMaxCount
      }
      case count if count <= taskMinCount => {
        println(s" number of tasks reached min of $taskMinCount")
        taskMinCount
      }
      case count => {
        println(s"setting number of tasks to $count for ${app.id}")
        count
      }
    }
  }

}
