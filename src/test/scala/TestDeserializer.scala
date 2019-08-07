import com.fasterxml.jackson.databind.JsonNode
import utils.JsonUtil

import scala.io.Source

object TestDeserializer extends App {
  val fileName = "person-data.json"
  val baseName = fileName.replaceAll("-\\d.*$", "")
  val schemaName = baseName.replaceAll("-", "_")
  val moduleName = fileName.replaceAll("\\.(xml|json)", ".tdex")
  val json = Source.fromResource("entitymodels/" +fileName).mkString

  val parsedJson = JsonUtil.fromJson[Map[String, JsonNode]](json)

  val jsonObj = JsonUtil.fromJson[Model](Source.fromResource("test.json").mkString)

  val l = List.empty
  println(l.isEmpty)
  println("hello")

}
