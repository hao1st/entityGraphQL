import sangria.ast.Document
import sangria.execution.deferred.DeferredResolver
import sangria.execution.{ErrorWithResolver, Executor, QueryAnalysisError}
import sangria.parser.{QueryParser, SyntaxError}
import sangria.parser.DeliveryScheme.Try
import sangria.marshalling.circe._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import io.circe._
import io.circe.optics.JsonPath._
import io.circe.parser._

import scala.io._
import scala.util.control.NonFatal
import scala.util.{Failure, Success}
import utils.GraphQLRequestUnmarshaller._
import sangria.slowlog.SlowLog
import utils.JsonUtil

object Server extends App with CorsSupport {
  implicit val system = ActorSystem("Entity-Server")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher

  def executeEntityGraphQL(query: Document, operationName: Option[String], variables: Json, tracing: Boolean) = {
    complete(
      Executor.execute(MLSchemaDefinition.EntitySchema, query, new EntityModelRepo,
        variables = if (variables.isNull) Json.obj() else variables,
        operationName = operationName,
        middleware = if (tracing) SlowLog.apolloTracing :: Nil else Nil)
        .map(OK → _)
        .recover {
          case error: QueryAnalysisError ⇒ BadRequest → error.resolveError
          case error: ErrorWithResolver ⇒ InternalServerError → error.resolveError
        })
  }

  def formatError(error: Throwable): Json = error match {
    case syntaxError: SyntaxError ⇒
      Json.obj("errors" → Json.arr(
      Json.obj(
        "message" → Json.fromString(syntaxError.getMessage),
        "locations" → Json.arr(Json.obj(
          "line" → Json.fromBigInt(syntaxError.originalError.position.line),
          "column" → Json.fromBigInt(syntaxError.originalError.position.column))))))
    case NonFatal(e) ⇒
      formatError(e.getMessage)
    case e ⇒
      throw e
  }

  def formatError(message: String): Json =
    Json.obj("errors" → Json.arr(Json.obj("message" → Json.fromString(message))))

  def readJsonFile(fileName: String) : Map[String, Object] = {
    val json = Source.fromFile(fileName).mkString
    val parsedJson = JsonUtil.fromJson[Map[String, Object]](json)

    parsedJson
  }

  val route: Route =
    optionalHeaderValueByName("X-Apollo-Tracing") { tracing ⇒
      path("entity") {
        get {
          explicitlyAccepts(`text/html`) {
            getFromResource("assets/playground.html")
          }/* ~
            parameters('query, 'operationName.?, 'variables.?) { (query, operationName, variables) ⇒
              QueryParser.parse(query) match {
                case Success(ast) ⇒
                  variables.map(parse) match {
                    case Some(Left(error)) ⇒ complete(BadRequest, formatError(error))
                    case Some(Right(json)) ⇒ executeEntityGraphQL(ast, operationName, json, tracing.isDefined)
                    case None ⇒ executeEntityGraphQL(ast, operationName, Json.obj(), tracing.isDefined)
                  }
                case Failure(error) ⇒ complete(BadRequest, formatError(error))
              }
            }*/
        } ~
        post {
          parameters('query.?, 'operationName.?, 'variables.?) { (queryParam, operationNameParam, variablesParam) ⇒
            entity(as[Json]) { body ⇒
              val query = queryParam orElse root.query.string.getOption(body) orElse Some(DEFAULT_MODEL_QUERY)
              val operationName = operationNameParam orElse root.operationName.string.getOption(body)
              val variablesStr = variablesParam orElse root.variables.string.getOption(body)

              query.map(QueryParser.parse(_)) match {
                case Some(Success(ast)) ⇒
                  variablesStr.map(parse) match {
                    case Some(Left(error)) ⇒ complete(BadRequest, formatError(error))
                    case Some(Right(json)) ⇒ executeEntityGraphQL(ast, operationName, json, tracing.isDefined)
                    case None ⇒ executeEntityGraphQL(ast, operationName, root.variables.json.getOption(body) getOrElse Json.obj(), tracing.isDefined)
                  }
                case Some(Failure(error)) ⇒ complete(BadRequest, formatError(error))
                case None ⇒ complete(BadRequest, formatError("No query to execute"))
              }
            } ~
              entity(as[Document]) { document ⇒
                variablesParam.map(parse) match {
                  case Some(Left(error)) ⇒ complete(BadRequest, formatError(error))
                  case Some(Right(json)) ⇒ executeEntityGraphQL(document, operationNameParam, json, tracing.isDefined)
                  case None ⇒ executeEntityGraphQL(document, operationNameParam, Json.obj(), tracing.isDefined)
                }
              }
          }
        } ~
        get {
          parameters('query.?, 'operationName.?, 'variables.?) { (queryParam, operationName, variables) ⇒
            //entity(as[Json]) { body ⇒
              val query = queryParam orElse Some(DEFAULT_ENTITIES_QUERY)
              query.map(QueryParser.parse(_)) match {
                case Some(Success(ast)) ⇒
                  variables.map(parse) match {
                    case Some(Left(error)) ⇒ complete(BadRequest, formatError(error))
                    case Some(Right(json)) ⇒ executeEntityGraphQL(ast, operationName, json, tracing.isDefined)
                    case None ⇒ executeEntityGraphQL(ast, operationName, Json.obj(), tracing.isDefined)
                  }
                case Some(Failure(error)) ⇒ complete(BadRequest, formatError(error))
                case None ⇒ complete(BadRequest, formatError("No query to execute"))
              }
            //}
          }
        }
      }
    } ~
    (get & pathEndOrSingleSlash) {
      redirect("/entity", PermanentRedirect)
    }

  Http().bindAndHandle(corsHandler(route), "0.0.0.0", sys.props.get("http.port").fold(4040)(_.toInt))

  lazy val DEFAULT_ENTITIES_QUERY =
    """
      query getEntities {
        Entities {
          names
        }
      }
    """

  lazy val DEFAULT_MODEL_QUERY =
    """
      query getEntityModel($fileName: String!) {
        EntityModel(fileName: $fileName) {
          name
          version
          id
          title
          description
          triples {
            subject
            predicate
            object
          }
          definitions {
            name
            description
            id
            primaryKey
            required
            rangeIndex
            pathRangeIndex
            elementRangeIndex
            wordLexicon
            namespace
            namespacePrefix
            properties {
              name
              type
              ref
              collation
              description
              item {
                type
                refParent
                ref
                collation
              }
            }
          }
        }
      }
    """
}
