import MLSchemaDefinition.EntitySchema
import io.circe._
import org.scalatest.{Matchers, WordSpec}
import sangria.ast.Document
import sangria.execution.Executor
import sangria.marshalling.circe._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class TestSchemaSpec extends WordSpec with Matchers {
  def executeQuery(query: Document, vars: Json = Json.obj()) = {
    val futureResult = Executor.execute(EntitySchema, query,
      variables = vars,
      userContext = new EntityModelRepo
    )

    Await.result(futureResult, 10.seconds)
  }

}
