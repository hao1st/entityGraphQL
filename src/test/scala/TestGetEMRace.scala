import sangria.macros._

import io.circe.Json
import io.circe.parser.parse

class TestGetEMRace extends TestSchemaSpec {
  "Entity Schema" should {
    "get an entity model from a json file (race-data.json)" in {
      val query =
        gql"""
          query testGetEntityModel($$fileName: String!) {
           EntityModel(fileName: $$fileName) {
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

      executeQuery(query, vars = Json.obj("fileName" → Json.fromString("race-data.json"))) should be(parse(
        """
        {
          "data": {
            "EntityModel": {
              "name": "race-data",
              "version": "0.0.2",
              "id": null,
              "title": "Race",
              "description": "This schema represents a Runner who runs Runs and has the potential of winning Races..It fixes datatypes and keys from version 0.0.1 and also incorporates a link to an external hierarchy (LDBC sports ontology)",
              "triples": null,
              "definitions": [
                {
                  "name": "Race",
                  "description": null,
                  "id": null,
                  "primaryKey": "raceIri",
                  "required": [
                    "name",
                    "courseLength"
                  ],
                  "rangeIndex": null,
                  "pathRangeIndex": null,
                  "elementRangeIndex": null,
                  "wordLexicon": null,
                  "namespace": null,
                  "namespacePrefix": null,
                  "properties": [
                    {
                      "name": "raceIri",
                      "type": "iri",
                      "ref": null,
                      "collation": null,
                      "description": "A unique identifier for a race.",
                      "item": null
                    },
                    {
                      "name": "name",
                      "type": "string",
                      "ref": null,
                      "collation": null,
                      "description": "The name of the race.",
                      "item": null
                    },
                    {
                      "name": "raceCategory",
                      "type": "object",
                      "ref": "http://example.org/ontologies/Running",
                      "collation": null,
                      "description": "A reference to an external taxonomy of race types.",
                      "item": null
                    },
                    {
                      "name": "comprisedOfRuns",
                      "type": "array",
                      "ref": null,
                      "collation": null,
                      "description": "An array of Runs that comprise the race.",
                      "item": {
                        "type": "object",
                        "refParent": null,
                        "ref": "#/definitions/Run",
                        "collation": null
                      }
                    },
                    {
                      "name": "wonByRunner",
                      "type": "object",
                      "ref": "#/definitions/Runner",
                      "collation": null,
                      "description": "The (single) winner of the race.  (rule) Should match the run of shortest duration.",
                      "item": {
                        "type": "object",
                        "refParent": null,
                        "ref": "#/definitions/Run",
                        "collation": null
                      }
                    },
                    {
                      "name": "courseLength",
                      "type": "decimal",
                      "ref": null,
                      "collation": null,
                      "description": "Length of the course in a scalar unit (decimal miles)",
                      "item": {
                        "type": "object",
                        "refParent": null,
                        "ref": "#/definitions/Run",
                        "collation": null
                      }
                    }
                  ]
                },
                {
                  "name": "Run",
                  "description": null,
                  "id": null,
                  "primaryKey": "id",
                  "required": [
                    "date",
                    "distance",
                    "runByRunner"
                  ],
                  "rangeIndex": [
                    "date",
                    "distance",
                    "duration",
                    "runByRunner"
                  ],
                  "pathRangeIndex": null,
                  "elementRangeIndex": null,
                  "wordLexicon": null,
                  "namespace": null,
                  "namespacePrefix": null,
                  "properties": [
                    {
                      "name": "id",
                      "type": "iri",
                      "ref": null,
                      "collation": null,
                      "description": "A unique iri for the run.",
                      "item": null
                    },
                    {
                      "name": "date",
                      "type": "date",
                      "ref": null,
                      "collation": null,
                      "description": "The date on which the run occurred.",
                      "item": null
                    },
                    {
                      "name": "distance",
                      "type": "decimal",
                      "ref": null,
                      "collation": null,
                      "description": "The distance covered, in a scalar value.",
                      "item": null
                    },
                    {
                      "name": "distanceLabel",
                      "type": "string",
                      "ref": null,
                      "collation": null,
                      "description": "The distance covered, in a conventional notation.",
                      "item": null
                    },
                    {
                      "name": "duration",
                      "type": "dayTimeDuration",
                      "ref": null,
                      "collation": null,
                      "description": "The duration of the run.  Optional because in some circumstances a run is not 'finished'",
                      "item": null
                    },
                    {
                      "name": "runByRunner",
                      "type": "object",
                      "ref": "#/definitions/Runner",
                      "collation": null,
                      "description": null,
                      "item": null
                    }
                  ]
                },
                {
                  "name": "Runner",
                  "description": null,
                  "id": null,
                  "primaryKey": "name",
                  "required": [
                    "name",
                    "age"
                  ],
                  "rangeIndex": null,
                  "pathRangeIndex": null,
                  "elementRangeIndex": null,
                  "wordLexicon": [
                    "name"
                  ],
                  "namespace": null,
                  "namespacePrefix": null,
                  "properties": [
                    {
                      "name": "id",
                      "type": "iri",
                      "ref": null,
                      "collation": null,
                      "description": "A unique IRI for a runner. Can be used to dedupe instances of runners.",
                      "item": null
                    },
                    {
                      "name": "name",
                      "type": "string",
                      "ref": null,
                      "collation": null,
                      "description": "The name of the runner.  In this early model, unique and a PK.",
                      "item": null
                    },
                    {
                      "name": "age",
                      "type": "decimal",
                      "ref": null,
                      "collation": null,
                      "description": "age, in decimal years.",
                      "item": null
                    },
                    {
                      "name": "gender",
                      "type": "string",
                      "ref": null,
                      "collation": null,
                      "description": "The gender of the runner (for the purposes of race categories.)",
                      "item": null
                    }
                  ]
                }
              ]
            }
          }
        }
        """).right.get)
    }
  }
}

