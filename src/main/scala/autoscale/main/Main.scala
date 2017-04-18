package autoscale.main

import autoscale.model._

/**
  * Created by vitorpaulonavancini on 02/02/17.
  */
object Main {
  def main(args: Array[String]) {
    var appsTimeline: List[List[MarathonApp]] = List(MarathonService.fetchAppsForScale())
    val t = new java.util.Timer()
    val task = new java.util.TimerTask {

      //sometimes usage is negative, dont know why yet but it makes no sense so im ignoring it
      //double and float??
      def isOverUsing(usagePercent: Double, maxUsagePercent: Float): Boolean = {
        if (usagePercent < 0) {
          return false
        }
        usagePercent > maxUsagePercent
      }

      def isUnderUsing(usagePercent: Double, maxUsagePercent: Float): Boolean = {
        if (usagePercent < 0) {
          return false
        }
        usagePercent < maxUsagePercent
      }

      def run() = {
        val appForScale: List[MarathonApp] = MarathonService.fetchAppsForScale()
        appsTimeline = appendToTimelineHead(appForScale, appsTimeline)


        appsTimeline.head.foreach((app: MarathonApp) => {
          //Im using getAutoscaleMode twice, here and in the MarathonApp.getLimits, maybe its a bad thing
          val mode = MarathonApp.getAutoscaleMode(app)
          val scalePolicy = MarathonApp.getScalePolicy(app)
          //this throws head empty exception when adding autoscale to a new app in marathon
          val appLastStats: MarathonApp = appsTimeline.tail.head.filter(_.id == app.id).head

          println(scalePolicy)
          println(mode)

          if (appLastStats.tasks.length == app.tasks.length) {
            mode match {
              case "CPU" => {
                val usage = MarathonApp.calculateCpuUsage(app.tasks, appLastStats.tasks)
                if (isOverUsing(usage, scalePolicy.max)) {
                  println(s"too much cpu for ${app.id}")
                  MarathonService.scaleApp(
                    app,
                    scalePolicy.scaleFactor,
                    scalePolicy.maxInstanceCount,
                    scalePolicy.minInstanceCount
                  )
                }
                if (isUnderUsing(usage, scalePolicy.min)) {
                  println(s"too little cpu for ${app.id}")
                  MarathonService.scaleApp(
                    app,
                    -scalePolicy.scaleFactor,
                    scalePolicy.maxInstanceCount,
                    scalePolicy.minInstanceCount
                  )
                }
              }
              case "MEM" => {
                val usage = MarathonApp.calculateMemUsage(app.tasks)
                if (isOverUsing(usage, scalePolicy.max)) {
                  println(s"too much mem for ${app.id}")
                  MarathonService.scaleApp(
                    app,
                    scalePolicy.scaleFactor,
                    scalePolicy.maxInstanceCount,
                    scalePolicy.minInstanceCount
                  )
                }
                if (isUnderUsing(usage, scalePolicy.min)) {
                  println(s"too little mem for ${app.id}")
                  MarathonService.scaleApp(
                    app,
                    -scalePolicy.scaleFactor,
                    scalePolicy.maxInstanceCount,
                    scalePolicy.minInstanceCount
                  )
                }
              }
            }
          }
        })
      }
    }

    t.schedule(task, 3000L, 3000L)
  }

  def appendToTimelineHead(apps: List[MarathonApp], appsTimeline: List[List[MarathonApp]]): List[List[MarathonApp]] = {
    appsTimeline.length match {
      case 1 => {
        apps :: appsTimeline
      }
      case 2 => {
        apps :: appsTimeline.dropRight(1)
      }
      case _ => {
        throw new IllegalArgumentException("should have one or two list of apps")
      }
    }
  }
}
