package autoscale.model
import org.scalatest.WordSpec
import org.scalamock.scalatest.MockFactory

/**
 * Created by vitorpaulonavancini on 07/02/17.
 */
class MarathonAppSpec  extends WordSpec with MockFactory {
  val marathonApiMock = mock[MarathonServiceTrait]
  val tasks:Seq[Task] = List(Task("id", "host"))
  //(marathonApiMock.getAppTasks _).expects("teste-id").returning(tasks)

  "A Marathon App" when {
    "configured with max 90% mem" should {
      " be overusing for more than 90% of mem usage" in {


//        val marathonApp = MarathonApp("teste-id", Map("autoscale" -> "true"));
//
//        println(marathonApp.tasks())
//        assert(marathonApp.isOverUsing == true)
      }
    }
  }
}
