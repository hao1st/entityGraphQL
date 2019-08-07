import sangria.schema.{Field, _}

/**
 * Defines a GraphQL schema for entity
 */
object MLSchemaDefinition {
  val Triple =
    ObjectType(
      "Triple",
      "Triple of an entity model",
      fields[Unit, Triple](
        Field("subject", StringType,
          Some("A triple subject of an entity model."),
          resolve = _.value.subject),
        Field("predicate", StringType,
          Some("A triple predicate of an entity model."),
          resolve = _.value.predicate),
        Field("object", StringType,
          Some("A triple object of an entity model."),
          resolve = _.value.`object`)
      ))

  val Item =
    ObjectType(
      "Item",
      "Item of a property",
      fields[EntityModelRepo, Item](
        Field("type", StringType,
          Some("Type of an array item."),
          resolve = _.value.`type`),
        Field("refParent", OptionType(StringType),
          Some("ref parent path of an array item."),
          resolve = _.value.refParent),
        Field("ref", OptionType(StringType),
          Some("ref path of an array item."),
          resolve = _.value.ref),
        Field("collation", OptionType(StringType),
          Some("collation of an array item."),
          resolve = _.value.collation)
      ))

  val Property =
    ObjectType(
      "Property",
      "The properties of an entity",
      () ⇒ fields[EntityModelRepo, Property](
        Field("name", StringType,
          Some("Name of an entity property."),
          resolve = _.value.name),
        Field("type", StringType,
          Some("Type of an entity property."),
          resolve = _.value.`type`),
        Field("ref", OptionType(StringType),
          Some("The ref path of an entity property"),
          resolve = _.value.ref),
        Field("description", OptionType(StringType),
          Some("The description of an entity property."),
          resolve = _.value.description),
        Field("collation", OptionType(StringType),
          Some("The collation of an entity property."),
          resolve = _.value.collation),
        Field("item", OptionType(Item),
          Some("Items of an entity property if type is array."),
          resolve = _.value.item)
      ))

  val EntityDefinition =
    ObjectType(
      "EntityDefinition",
      "The definition of an entity",
      fields[Unit, EntityDefinition](
        Field("name", StringType,
          Some("Name of an entity."),
          resolve = _.value.name),
        Field("id", OptionType(StringType),
          Some("The absolute resource baseurl of the entity."),
          resolve = _.value.id),
        Field("description", OptionType(StringType),
          Some("The description of an entity."),
          resolve = _.value.description),
        Field("primaryKey", OptionType(StringType),
          Some("The primary key of an entity."),
          resolve = _.value.primaryKey),
        Field("required", OptionType(ListType(StringType)),
          Some("Required fields if any."),
          resolve = _.value.required map (e ⇒ e)),
        Field("rangeIndex", OptionType(ListType(StringType)),
          Some("RangeIndex fields if any."),
          resolve = _.value.rangeIndex map (e ⇒ e)),
        Field("pathRangeIndex", OptionType(ListType(StringType)),
          Some("PathRangeIndex fields if any."),
          resolve = _.value.pathRangeIndex map (e ⇒ e)),
        Field("elementRangeIndex", OptionType(ListType(StringType)),
          Some("ElementRangeIndex fields if any."),
          resolve = _.value.elementRangeIndex map (e ⇒ e)),
        Field("wordLexicon", OptionType(ListType(StringType)),
          Some("WordLexicon fields if any."),
          resolve = _.value.wordLexicon map (e ⇒ e)),
        Field("namespace", OptionType(StringType),
          Some("Namespace of an entity."),
          resolve = _.value.namespace),
        Field("namespacePrefix", OptionType(StringType),
          Some("Namespace prefix of an entity."),
          resolve = _.value.namespacePrefix),
        Field("properties", OptionType(ListType(Property)),
                  Some("Properties of an entity."),
                  resolve = _.value.properties map (e ⇒ e))
      ))

  val Entities =
    ObjectType(
      "Entities",
      "All entity names.",
      fields[EntityModelRepo, Entities](
        Field("names", OptionType(ListType(StringType)),
          Some("entity names"),
          resolve = _.value.names map (e ⇒ e))))

  val EntityModel =
    ObjectType(
      "EntityModel",
      "An Entity Model.",
      fields[EntityModelRepo, EntityModel](
        Field("name", StringType,
          Some("The name of the entity model."),
          resolve = _.value.name),
        Field("version", OptionType(StringType),
          Some("The version of the entity model."),
          resolve = _.value.version),
        Field("id", OptionType(StringType),
          Some("The absolute resource baseurl of the entity model."),
          resolve = _.value.id),
        Field("title", OptionType(StringType),
          Some("The title of the entity model, or an empty string if not defined."),
          resolve = _.value.title),
        Field("description", OptionType(StringType),
          Some("The description of the entity model."),
          resolve = _.value.description),
        Field("baseUri", OptionType(StringType),
          Some("The baseUri of the entity model."),
          resolve = _.value.baseUri),
        Field("triples", OptionType(ListType(Triple)),
          Some("The triples of the entity model."),
          resolve = ctx => ctx.value.triples map (e ⇒ e)),
        Field("definitions", OptionType(ListType(EntityDefinition)),
          Some("The entity model definitions."),
          resolve = _.value.definitions map (e ⇒ e))
      ))

  val EM_ENTITY_NAME = Argument("entityName", StringType, description = "name of an entity")
  val TITLE = Argument("title", StringType, description = "title of an entity model")
  val EM_FILE_NAME = Argument("fileName", StringType, description = "json file name of an entity model")

  val Query = ObjectType(
    "Query",
    fields[EntityModelRepo, Unit](
      Field("Entities", OptionType(Entities),
        arguments = Nil,
        resolve = ctx ⇒ ctx.ctx.getEntities()),
      Field("EntityModel", EntityModel,
        arguments = EM_FILE_NAME :: Nil,
        resolve = ctx ⇒ ctx.ctx.getEntityModel(ctx arg EM_FILE_NAME)),
      Field("EntityDefinitionByName", OptionType(EntityDefinition),
        arguments = EM_FILE_NAME :: EM_ENTITY_NAME :: Nil,
        resolve = ctx ⇒ ctx.ctx.getEntityDefinitionByName(ctx arg EM_FILE_NAME, ctx arg EM_ENTITY_NAME))
  ))

  val EntitySchema = Schema(Query)
}
