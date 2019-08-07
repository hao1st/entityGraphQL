import java.io.File
import java.nio.file.Paths

import com.fasterxml.jackson.databind.JsonNode
import utils.JsonUtil

import scala.io.Source

case class Info(version: String, description: Option[String], title: Option[String], baseUri: Option[String])

case class EntityProperty(primaryKey: String, nameSpace: String, required: Seq[String])

case class Definition (name: String, properties: EntityProperty)

case class Model (info: Info, definitions: Seq[Definition])

case class Entities(names: Option[List[String]])

//case classes
case class EntityModel (
  name: String,
  version: String,
  id: Option[String],
  title: Option[String],
  description: Option[String],
  baseUri: Option[String],
  triples: Option[List[Triple]],
  definitions: Option[List[EntityDefinition]])

case class Triple (
  subject: String,
  predicate: String,
  `object`: String)

case class EntityDefinition (
  name: String,
  id: Option[String],
  description: Option[String],
  primaryKey: Option[String],
  required: Option[List[String]],
  rangeIndex: Option[List[String]],
  pathRangeIndex: Option[List[String]],
  elementRangeIndex: Option[List[String]],
  wordLexicon: Option[List[String]],
  namespace: Option[String],
  namespacePrefix: Option[String],
  properties: Option[List[Property]])

case class Property(name: String, `type`: String, ref: Option[String], description: Option[String], collation: Option[String], item: Option[Item])

case class Item(`type`: String, refParent: Option[String], ref: Option[String], collation: Option[String])

/**
 * currently expose two public interfaces for quering entity model
 * 1. get the Entity model by passing one variable (filename)
 * 2. get an entity definition in a model by passing two variables (filename and an entity name)
 *
 *
 */
class EntityModelRepo {

  def getEntities(): Entities = {
    val path = new File(".").getCanonicalPath
    val currDir = Paths.get(".").toAbsolutePath.toString
    val dir = new File(currDir + EntityModelRepo.MODEL_RES_FOLDER_HOME);

    val files = getRecursiveListOfFiles(dir)

    if (!files.isEmpty) {
      Entities(Some(files.map(_.getName).filter(n => n.endsWith("json")).sorted.toList))
    } else {
      Entities(None)
    }
  }

  def getEntityModel(fileName: String): EntityModel = {
    parseMLEntity(fileName)
  }

  private def getRecursiveListOfFiles(dir: File): Array[File] = {
    val these = dir.listFiles
    these ++ these.filter(_.isDirectory).flatMap(getRecursiveListOfFiles)
  }

  private def parseTriples(infoNode: JsonNode): Option[List[Triple]] = {
    var triples: Option[List[Triple]] = None
    if (!infoNode.get("triple").isNull() && infoNode.get("triple").isArray) {
      var triple: List[Triple] = Nil
      infoNode.get("triple").forEach(e => {
        val itTriple = e.fieldNames()
        var sub = ""
        var pre = ""
        var obj = "";

        while (itTriple.hasNext()) {
          itTriple.next() match {
            case "subject" => sub = e.get("subject").asText()
            case "predicate" => pre = e.get("predicate").asText()
            case "object" => obj = e.get("object").asText()
          }
        }
        if (!sub.isEmpty) {
          triple :+= Triple(sub, pre, obj)
        }
      })
      if (triple.size > 0) triples = Some(triple)
    }
    triples
  }

  private def parseItems(propCNode: JsonNode, defId: Option[String], id: Option[String]): Item = {
    var items: Item = null
    var itemType: String = "object"
    var itemParRef: Option[String] = if (defId == None) id else defId
    var itemRef: Option[String] = None
    var itemCollation: Option[String] = None

    val itemNode: JsonNode = propCNode.get("items")
    if (itemNode != null) {
      var itemit = itemNode.fieldNames()
      while (itemit.hasNext()) {
        itemit.next() match {
          case "type" => itemType = itemNode.get("type").asText
          case "datatype" => itemType = if ("object".equals(itemType) || itemType.isEmpty()) itemNode.get("datatype").asText else itemType
          case "$ref" => itemRef = Some(itemNode.get("$ref").asText)
          case "collation" => itemCollation = Some(itemNode.get("collation").asText)
          case _ => None
        }
      }
      items = Item(itemType, itemParRef, itemRef, itemCollation)
    }
    items
  }

  private def parseProperties(propNode: JsonNode, defId: Option[String], id: Option[String]): List[Property] = {
    var props: List[Property] = Nil
    var items: Item = null
    var propit = propNode.fieldNames()
    /*
      name: String,
      `type`: String,
      id: Option[String],
      description: Option[String],
      collation: Option[String],
      item: Option[Item],
     */
    while (propit.hasNext()) {
      val propName = propit.next()
      var propType: String = "object"
      var propRef: Option[String] = None
      var propDesc: Option[String] = None
      var propCollation: Option[String] = None

      val propCNode = propNode.get(propName)
      val propcit = propCNode.fieldNames()
      while (propcit.hasNext) {
        propcit.next() match {
          case "type" => propType = propCNode.get("type").asText
          case "datatype" => propType = if ("object".equals(propType) || propType.isEmpty()) propCNode.get("datatype").asText else propType
          /*
          "wonByRunner": {
              "$ref":"#/definitions/Runner",
              "description":"The (single) winner of the race."
          },
           */
          case "$ref" => propRef = Some(propCNode.get("$ref").asText)
          case "description" => propDesc = Some(propCNode.get("description").asText)
          case "collation" => propCollation = Some(propCNode.get("collation").asText)
          /*`type`: String,
            refParent: Option[String],
            ref: Option[String],
            collation: Option[String]
            //properties: Option[List[PropertyTrait]] */
          case _ => None
        }
      }
      if (propType.isEmpty()) { //make sure the type is set
        propType = "object"
      }
      if ("array".equals(propType)) {
        items = parseItems(propCNode, defId, id)
        //has items definition
        /*
         "friends": {
          "datatype": "array",
          "items" : {
            "$ref": "#/definitions/Person"
          }
        }*/
        var itemType: String = "object"
        var itemParRef: Option[String] = if (defId == None) id else defId
        var itemRef: Option[String] = None
        var itemCollation: Option[String] = None

        val itemNode: JsonNode = propCNode.get("items")
        if (itemNode != null) {
          var itemit = itemNode.fieldNames()
          while (itemit.hasNext()) {
            itemit.next() match {
              case "type" => itemType = itemNode.get("type").asText
              case "datatype" => itemType = if ("object".equals(itemType) || itemType.isEmpty()) itemNode.get("datatype").asText else itemType
              case "$ref" => itemRef = Some(itemNode.get("$ref").asText)
              case "collation" => itemCollation = Some(itemNode.get("collation").asText)
              case _ => None
            }
          }
          items = Item(itemType, itemParRef, itemRef, itemCollation)
        }
      }
      props :+= Property(propName, propType, propRef, propDesc, propCollation, if (items != null) Some(items) else None)
    }
    props
  }

  private def constructStringList(node: JsonNode, fieldName: String): Option[List[String]] = {
    var lstRes: List[String] = Nil
    if (!node.get(fieldName).isNull() && node.get(fieldName).isArray) {
      node.get(fieldName).forEach(e => {
        lstRes :+= e.asText()
      })
    }
    if (!lstRes.isEmpty) Some(lstRes) else None
  }

  private def parseDefinition(defNode: JsonNode, id: Option[String]): List[EntityDefinition] = {
    var defs: List[EntityDefinition] = Nil
    val defit = defNode.fieldNames()
    while (defit.hasNext()) {
      val defName = defit.next()
      var defId: Option[String] = None
      var defPk: Option[String] = None
      var defDesc: Option[String] = None
      var defReq: Option[List[String]] = None
      var defRI: Option[List[String]] = None
      var defPRI: Option[List[String]] = None
      var defERI: Option[List[String]] = None
      var defWL: Option[List[String]] = None
      var defNS: Option[String] = None
      var defNSPrefix: Option[String] = None
      var props: List[Property] = Nil

      val defChildNode = defNode.get(defName)
      val defcit = defChildNode.fieldNames()
      while (defcit.hasNext) {
        defcit.next() match {
          case "$id" => defId = Some(defChildNode.get("$id").asText)
          case "description" => defDesc = Some(defChildNode.get("description").asText)
          case "primaryKey" => defPk = Some(defChildNode.get("primaryKey").asText)
          case "required" =>  defReq = constructStringList(defChildNode, "required")
          case "rangeIndex" =>  defRI = constructStringList(defChildNode, "rangeIndex")
          case "pathRangeIndex" =>  defPRI = constructStringList(defChildNode, "pathRangeIndex")
          case "elementRangeIndex" =>  defERI = constructStringList(defChildNode, "elementRangeIndex")
          case "wordLexicon" =>  defWL = constructStringList(defChildNode, "wordLexicon")
          case "namespace" => defNS = Some(defChildNode.get("namespace").asText)
          case "namespacePrefix" => defNSPrefix = Some(defChildNode.get("namespacePrefix").asText)
          case "properties" => props = parseProperties(defChildNode.get("properties"), defId, id)
          case _ => None
        }
      }
      defs :+= EntityDefinition(defName, defId, defDesc, defPk, defReq, defRI, defPRI, defERI, defWL,
        defNS, defNSPrefix, if (!props.isEmpty) Some(props) else None)
    }
    defs
  }

  private def parseModelName(fileName: String): String = {
    var lastPos = fileName.lastIndexOf("/")
    val baseName = if (lastPos != -1) fileName.substring(lastPos + 1).replaceAll("-\\d.*$", "")
                      else fileName.replaceAll("-\\d.*$", "")
    baseName.replaceAll("\\.(xml|json)$", "")
  }

  private def parseMLEntityFile(fileName: String) = {
    val currentDirectory = new java.io.File(".").getCanonicalPath
    val json = Source.fromFile(currentDirectory + EntityModelRepo.MODEL_RES_FOLDER_HOME + fileName).mkString;

    //val json = Source.fromResource(fileName).mkString;
    //Unmarshallable(json).fromJson[Map[String, JsonNode]]()
    JsonUtil.fromJson[Map[String, JsonNode]](json)
  }

  private def parseMLEntity(fileName: String): EntityModel = {
    var lastPos = fileName.lastIndexOf("/")

    val baseName = if (lastPos != -1) fileName.substring(lastPos + 1).replaceAll("-\\d.*$", "")
                      else fileName.replaceAll("-\\d.*$", "")
    val modelName = baseName.replaceAll("\\.(xml|json)$", "")

    val parsedJson = parseMLEntityFile(fileName)

    val infoNode: JsonNode = parsedJson.getOrElse("info", JsonUtil.createObjectNode)
    val defNode: JsonNode = parsedJson.getOrElse("definitions", JsonUtil.createObjectNode)

    val id: Option[String] = if (parsedJson.contains("$id")) Some(parsedJson.get("$id").get.asText()) else None

    var triples: Option[List[Triple]] = None
    var title : Option[String] = None
    var desc : Option[String] = None
    var baseuri : Option[String] = None
    var version = "0.0.1"
    val it = infoNode.fieldNames()
    while (it.hasNext()) {
      it.next() match {
        case "title" => title = Some(infoNode.get("title").asText)
        case "version" => version = infoNode.get("version").asText
        case "description" => desc = Some(infoNode.get("description").asText)
        case "baseUri" => baseuri = Some(infoNode.get("baseUri").asText)
        case "triple" => triples = parseTriples(infoNode)
        case _ => None
      }
    }

    var defs = parseDefinition(defNode, id)

    EntityModel(modelName,
      version,
      id,
      title,
      desc,
      baseuri,
      triples, if (!defs.isEmpty) Some(defs) else None)
  }

  def getEntityDefinitionByName(fileName: String, entityName: String): Option[EntityDefinition] = {
    val parsedJson = parseMLEntityFile(fileName)

    val defNode: JsonNode = parsedJson.getOrElse("definitions", JsonUtil.createObjectNode)

    val id: Option[String] = if (parsedJson.contains("$id")) Some(parsedJson.get("$id").get.asText()) else None
    var defs = parseDefinition(defNode, id)

    if (!defs.isEmpty) defs.find(e => e.name.equals(entityName)) else None
  }
}

object EntityModelRepo {
  final val MODEL_RES_FOLDER_HOME = "/src/test/resources/entitymodels/"
/*  val entityModels = List(
    EntityModel(name = "SimplestEntityModel", version = "0.0.1", id = None, title = Some("The simplest Entity Model"),
      description = None, baseUri = None, /* triple = None, */ triple = Some(List(Triple(subject = "sub", predicate="pred", `object`="obj"))),
      definitions = None /*Some(List(EntityDefinition(name = "Customer", id = None,
        description = None, primaryKey = Some("CustomerID"), required = None, rangeIndex = None, pathRangeIndex = None,
        elementRangeIndex = None, wordLexicon = None, namespace = None, namespacePrefix = None, properties = None)) */
    ))*/
}
