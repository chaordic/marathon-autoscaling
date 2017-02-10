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


      def isOverUsingCPU(cpuUsage: Double, maxUsagePercent: Int): Boolean ={
        return cpuUsage > maxUsagePercent
      }

      def run() = {
        val scale: List[MarathonApp] = MarathonService.fetchAppsForScale()
        appsTimeline = appendToTimelineHead(scale, appsTimeline)
        appsTimeline.head.foreach((app:MarathonApp) => {
          println(MarathonApp.calculateMemUsage(app.tasks))

          val appLastStats: MarathonApp = appsTimeline.tail.head.filter(_.id == app.id).head
          if(isOverUsingCPU(MarathonApp.calculateCpuUsage(app.tasks, appLastStats.tasks), 90)){
            print("too much cpu")
          }
          println(MarathonApp.calculateCpuUsage(app.tasks, appLastStats.tasks))
        })

        //        apps.foreach((app: MarathonApp) => {
//          if (app.isOverUsing) {
//            MarathonService.scaleApp(app.id, app.scalePolicy)
//            println("up")
//          } else if (app.isUnderusing) {
//            // MarathonService.scaleApp(app.id, -app.scalePolicy)
//          }
//        })
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

//
//  def isOverUsing(maxCpu: Float, maxMem: Float, mode: String): Boolean = {
//    val cpuTime = averageUsages()
//    mode match {
//      case "AND" => cpuAndMem.cpuTime > maxCpu && cpuAndMem.memUsage > maxMem
//      case "OR" => cpuAndMem.cpuTime > maxCpu || cpuAndMem.memUsage > maxMem
//      case _ => false
//    }
//  }
//
//  def isUnderusing(maxCpu: Float, maxMem: Float, mode: String): Boolean = {
//    val cpuAndMem: CpuAndMem = averageUsages()
//    println(cpuAndMem.cpuTime)
//    println(cpuAndMem.memUsage)
//
//    mode match {
//      case "AND" => cpuAndMem.cpuTime < maxCpu && cpuAndMem.memUsage > maxMem
//      case "OR" => cpuAndMem.cpuTime < maxCpu || cpuAndMem.memUsage > maxMem
//      case _ => false
//    }
//  }

}
