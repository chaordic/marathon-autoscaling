package autoscale.model

import autoscale.model.MarathonService.TaskWithStats
import org.json4s.DefaultFormats

/**
  * Created by vitorpaulonavancini on 02/02/17.
  */
case class MarathonApp(id: String, tasks: Seq[TaskWithStats])
object MarathonApp {

  def calculateCpuUsage(tasksTimestamp1: Seq[TaskWithStats], tasksTimestamp2: Seq[TaskWithStats]): Double = {
    val zip: Seq[(TaskWithStats, TaskWithStats)] = tasksTimestamp1.zip(tasksTimestamp2)

    zip.foldLeft(0.0)((sum: Double, taskPair: (TaskWithStats, TaskWithStats)) => {
      val stats1: Statistics = taskPair._1.stats.statistics
      val stats2: Statistics = taskPair._2.stats.statistics

      val cpuUsageDelta = (stats2.cpusSystemTimeSecs + stats2.cpusUserTimeSecs) - (stats1.cpusSystemTimeSecs + stats1.cpusUserTimeSecs)
      val timeStampDelta = stats2.timestamp - stats1.timestamp
      val cpuUsage = cpuUsageDelta / timeStampDelta
      val cpuTime = (cpuUsage / stats1.cpusLimit) * 100.0

      sum + cpuTime
    })
  }

  def calculateMemUsage(tasks: Seq[TaskWithStats]): Float = {
    tasks.foldLeft(0f)((acc, task) => {
      val memUsageA = task.stats.statistics.memRssBytes / task.stats.statistics.memLimitBytes
      acc + memUsageA
    })
  }
}

