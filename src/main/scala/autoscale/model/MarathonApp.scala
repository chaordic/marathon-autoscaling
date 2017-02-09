package autoscale.model

import autoscale.model.MarathonService.TaskWithStats
import org.json4s.{DefaultFormats}

/**
  * Created by vitorpaulonavancini on 02/02/17.
  */


case class MarathonApp(id:String, tasks:Seq[TaskWithStats]) {
  implicit val formats = DefaultFormats
  val scalePolicy = 1

  case class CpuAndMem(cpuTime: Double, memUsage: Float)

  def averageUsages(): CpuAndMem = {
    val cpuAndMemSum = tasks.foldLeft(CpuAndMem(0f, 0f))((sum: CpuAndMem, task: TaskWithStats) => {
      val stats1: Statistics = task.stats._1.statistics
      val stats2: Statistics = task.stats._2.statistics

      val cpuUsageDelta = (stats2.cpusSystemTimeSecs + stats2.cpusUserTimeSecs) - (stats1.cpusSystemTimeSecs + stats1.cpusUserTimeSecs)
      val timeStampDelta = stats2.timestamp - stats1.timestamp
      val cpuUsage = cpuUsageDelta / timeStampDelta
      val cpuTime = (cpuUsage / stats1.cpusLimit) * 100

      val memUsage = stats2.memRssBytes / stats2.memLimitBytes
      CpuAndMem(sum.cpuTime + cpuTime, sum.memUsage + memUsage)
    })
    CpuAndMem(cpuAndMemSum.cpuTime / tasks.length, cpuAndMemSum.memUsage / tasks.length)
  }

  def getTriggerMode(): String = {
    return "OR"
  }

  def isOverUsing(maxCpu:Float, maxMem:Float, mode:String): Boolean = {
    val cpuAndMem: CpuAndMem = averageUsages()
    mode match {
      case "AND" => cpuAndMem.cpuTime > maxCpu && cpuAndMem.memUsage > maxMem
      case "OR" => cpuAndMem.cpuTime > maxCpu || cpuAndMem.memUsage > maxMem
      case _ => false
    }
  }

  def isUnderusing(maxCpu:Float, maxMem:Float): Boolean = {
    val cpuAndMem: CpuAndMem = averageUsages()
    println(cpuAndMem.cpuTime)
    println(cpuAndMem.memUsage)

    val mode: String = getTriggerMode()
    mode match {
      case "AND" => cpuAndMem.cpuTime < maxCpu && cpuAndMem.memUsage > maxMem
      case "OR" => cpuAndMem.cpuTime < maxCpu || cpuAndMem.memUsage > maxMem
      case _ => false
    }
  }
}
