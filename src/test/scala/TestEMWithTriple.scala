import sangria.macros._

import io.circe.Json
import io.circe.parser.parse

class TestEMWithTriple extends TestSchemaSpec {
  "Entity schema with triple" should {
    "get an entity info from a json file with triple definition" in {
      val query =
        gql"""
          query testGetEntityModelInfoWithTriple($$fileName: String!) {
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
          }
        }
       """

      executeQuery(query, vars = Json.obj("fileName" → Json.fromString("es-customer-pii.json"))) should be(parse(
        """
          {
            "data": {
              "EntityModel": {
                "name": "es-customer-pii",
                "version": "0.0.1",
                "id": null,
                "title": "Secure",
                "description": "A model that contains extension metadata to indicate secured information.  The model has Orders and Customers, with sensitive information stored in the customer record.",
                "triples": [
                  {
                    "subject": "http://marklogic.com/entity-services/example-els/Secure-0.0.1/Customer/email",
                    "predicate": "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
                    "object": "http://marklogic.com/entity-services/example-els/policy/PersonallyIdentifiableInformationProperty"
                  },
                  {
                    "subject": "http://marklogic.com/entity-services/example-els/Secure-0.0.1/Customer/ssn",
                    "predicate": "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
                    "object": "http://marklogic.com/entity-services/example-els/policy/PersonallyIdentifiableInformationProperty"
                  }
                ]
              }
            }
          }
           """).right.get)
    }
  }
}
