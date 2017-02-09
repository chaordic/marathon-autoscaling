package autoscale.main

import autoscale.model._

/**
  * Created by vitorpaulonavancini on 02/02/17.
  */
object Main {
  def main(args: Array[String]) {
    val t = new java.util.Timer()
    val task = new java.util.TimerTask {
      def run() = checkAppsForScale
    }
    t.schedule(task, 3000L, 3000L)
  }

  def checkAppsForScale = {
    val apps: Seq[MarathonApp] = MarathonService.fetchAppsForScale()
    apps.foreach((app: MarathonApp) => {
      if (app.isOverUsing) {
        MarathonService.scaleApp(app.id, app.scalePolicy)
        println("up")
      } else if (app.isUnderusing) {
       // MarathonService.scaleApp(app.id, -app.scalePolicy)
      }
    })
  }
}


