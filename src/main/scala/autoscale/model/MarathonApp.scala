package autoscale.model


import autoscale.model.MarathonService.TaskWithStats
import scala.util.Try

/**
  * Created by vitorpaulonavancini on 02/02/17.
  */
case class MarathonApp(id: String, tasks: Seq[TaskWithStats], labels: AutoscaleLabels)

case class ScalePolicy(max: Float, min: Float, scaleFactor: Int = 1, maxInstanceCount: Int, minInstanceCount: Int)

object MarathonApp {
  def calculateCpuUsage(tasksTimestamp1: Seq[TaskWithStats], tasksTimestamp2: Seq[TaskWithStats]): Double = {
    val zip: Seq[(TaskWithStats, TaskWithStats)] = tasksTimestamp1.zip(tasksTimestamp2)

    val cpuUsagemSum = zip.foldLeft(0.0)((sum: Double, taskPair: (TaskWithStats, TaskWithStats)) => {

      val anyTaskEmpty: Boolean = taskPair._1.stats.isEmpty || taskPair._2.stats.isEmpty
      val cpu = if (anyTaskEmpty) 0
      else cpuInDeltaTime(
        taskPair._1.stats.get.statistics,
        taskPair._2.stats.get.statistics
      )
      sum + cpu
    })
    //throws number format exception sometimes
    val double = BigDecimal(cpuUsagemSum / tasksTimestamp1.length).setScale(2, BigDecimal.RoundingMode.HALF_DOWN).toDouble
    println(double)
    double
  }

  def cpuInDeltaTime(stats1: Statistics, stats2: Statistics): Double = {
    val cpuUsageDelta = (stats2.cpusSystemTimeSecs + stats2.cpusUserTimeSecs) - (stats1.cpusSystemTimeSecs + stats1.cpusUserTimeSecs)
    val timeStampDelta = stats2.timestamp - stats1.timestamp
    val cpuUsage = cpuUsageDelta / timeStampDelta
    val cpuTime = (cpuUsage / stats1.cpusLimit) * 100.0

    cpuTime
  }

  def calculateMemUsage(tasks: Seq[TaskWithStats]): Double = {
    val memUsageSum = tasks.foldLeft(0.0)((acc, task) => {
      val statistics = task.stats.getOrElse(return 0).statistics

      val memUsage = statistics.memRssBytes / statistics.memLimitBytes * 100
      acc + memUsage
    })
    val double = BigDecimal(memUsageSum / tasks.length).setScale(2, BigDecimal.RoundingMode.HALF_DOWN).toDouble
    println(double)
    double
  }

  def getAutoscaleMode(marathonApp: MarathonApp): String = {
    //gonna allow either cpu or mem, not both, so if both cpu and mem are set, CPU is going to be returned
    //if nothing is set, MEM is going to be returned
    val cpuLabelPresent: Boolean = marathonApp.labels.maxCpuPercent.nonEmpty || marathonApp.labels.minCpuPercent.nonEmpty
    if (cpuLabelPresent) "CPU" else "MEM"
  }

  def tryFloat(stringOption: Option[String], default: Float): Float = {
    Try(stringOption.get.toFloat).getOrElse(default)
  }

  def tryInt(stringOption: Option[String], default: Int): Int = {
    Try(stringOption.get.toInt).getOrElse(default)
  }

  val MAX_DEFAULT: Float = 100f
  val MIN_DEFAULT: Float = 20f
  val SCALE_FACTOR_DEFAULT: Int = 1
  val MIN_INSTANCE_DEFAULT: Int = 1

  def getScalePolicy(marathonApp: MarathonApp): ScalePolicy = {
    val labels: AutoscaleLabels = marathonApp.labels
    val maxInstanceDefault: Int = marathonApp.tasks.length * 2

    getAutoscaleMode(marathonApp) match {
      case "CPU" => {
        ScalePolicy(
          tryFloat(labels.maxCpuPercent, MAX_DEFAULT),
          tryFloat(labels.minCpuPercent, MIN_DEFAULT),
          tryInt(labels.scaleFactor, SCALE_FACTOR_DEFAULT),
          tryInt(labels.maxInstanceCount, maxInstanceDefault),
          tryInt(labels.minInstanceCount, MIN_INSTANCE_DEFAULT)
        )
      }
      case "MEM" => {
        ScalePolicy(
          tryFloat(labels.maxMemoryPercent, MAX_DEFAULT),
          tryFloat(labels.minMemoryPercent, MIN_DEFAULT),
          tryInt(labels.scaleFactor, SCALE_FACTOR_DEFAULT),
          tryInt(labels.maxInstanceCount, maxInstanceDefault),
          tryInt(labels.minInstanceCount, MIN_INSTANCE_DEFAULT)
        )
      }
    }

  }
}

