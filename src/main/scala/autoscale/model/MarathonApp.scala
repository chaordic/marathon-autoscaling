package autoscale.model

import autoscale.model.MarathonService.TaskWithStats
import org.json4s.{DefaultFormats}

/**
  * Created by vitorpaulonavancini on 02/02/17.
  */


case class MarathonApp(id:String, tasks:Seq[TaskWithStats]) {
  implicit val formats = DefaultFormats
  val maxCpu = 0.9
  val maxMem = 0.8

  case class CpuAndMem(cpuTime: Float, memUsage: Float)

  def averageUsages(): CpuAndMem = {
    val appTasks: Seq[Task] = List()
    val cpuAndMemSum: CpuAndMem = appTasks.foldLeft(CpuAndMem(0f, 0f))((sum: CpuAndMem, task: Task) => {

      val stats: Statistics = MarathonService.getTasksStats(task).statistics
      val cpuTime = stats.cpusSystemTimeSecs + stats.cpusUserTimeSecs
      val memUsage = (stats.memRssBytes / stats.memLimitBytes) * 100
      CpuAndMem(sum.cpuTime + cpuTime, sum.memUsage + memUsage)
    })
    println(cpuAndMemSum.cpuTime)
    CpuAndMem(cpuAndMemSum.cpuTime / appTasks.length, cpuAndMemSum.memUsage / appTasks.length)
  }

  def getTriggerMode(): String = {
    return "AND"
  }

  def isOverUsing: Boolean = {
    val cpuAndMem: CpuAndMem = averageUsages()
    val mode: String = getTriggerMode()
    mode match {
      case "AND" => cpuAndMem.cpuTime > maxCpu && cpuAndMem.memUsage > maxMem
      case "OR" => cpuAndMem.cpuTime > maxCpu || cpuAndMem.memUsage > maxMem
      case _ => false
    }
  }

  def isUnderusing: Boolean = {
    val cpuAndMem: CpuAndMem = averageUsages()
    val mode: String = getTriggerMode()
    mode match {
      case "AND" => cpuAndMem.cpuTime < maxCpu && cpuAndMem.memUsage > maxMem
      case "OR" => cpuAndMem.cpuTime < maxCpu || cpuAndMem.memUsage > maxMem
      case _ => false
    }
  }
}
