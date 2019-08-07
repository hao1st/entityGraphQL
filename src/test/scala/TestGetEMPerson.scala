import sangria.macros._

import io.circe.Json
import io.circe.parser.parse

class TestGetEMPerson extends TestSchemaSpec {
  "Entity Schema" should {
    "get an entity model from a json file" in {
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

      executeQuery(query, vars = Json.obj("fileName" → Json.fromString("person-data.json"))) should be(parse(
        """
        {
           "data": {
             "EntityModel": {
               "name": "person-data",
               "version": "0.0.1",
               "id": "http://example.org/Person",
               "title": "Person",
               "description": "A model of a person, to demonstrate several extractions",
               "triples": null,
               "definitions": [
                 {
                   "name": "Person",
                   "description": null,
                   "id": null,
                   "primaryKey": "id",
                   "required": [
                     "firstName",
                     "lastName",
                     "fullName"
                   ],
                   "rangeIndex": null,
                   "pathRangeIndex": null,
                   "elementRangeIndex": null,
                   "wordLexicon": null,
                   "namespace": "http://example.org/example-person",
                   "namespacePrefix": "p",
                   "properties": [
                     {
                       "name": "id",
                       "type": "string",
                       "ref": null,
                       "collation": null,
                       "description": null,
                       "item": null
                     },
                     {
                       "name": "firstName",
                       "type": "string",
                       "ref": null,
                       "collation": null,
                       "description": null,
                       "item": null
                     },
                     {
                       "name": "lastName",
                       "type": "string",
                       "ref": null,
                       "collation": null,
                       "description": null,
                       "item": null
                     },
                     {
                       "name": "fullName",
                       "type": "string",
                       "ref": null,
                       "collation": null,
                       "description": null,
                       "item": null
                     },
                     {
                       "name": "friends",
                       "type": "array",
                       "ref": null,
                       "collation": null,
                       "description": null,
                       "item": {
                         "type": "object",
                         "refParent": "http://example.org/Person",
                         "ref": "#/definitions/Person",
                         "collation": null
                       }
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
