package autoscale.main
import autoscale.model._

/**
  * Created by vitorpaulonavancini on 02/02/17.
  */
object Main {
   def main(args: Array[String]) {
      val scale: Seq[MarathonApp] = MarathonService.fetchAppsForScale()
      println(scale.head.averageUsages())
   }
}


