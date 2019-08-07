## Sangria akka-http Example (Entity)

An example [GraphQL](https://graphql.org) server written with [akka-http](https://github.com/akka/akka-http), 
[circe](https://github.com/circe/circe) and [sangria](https://github.com/sangria-graphql/sangria).

After starting the server with

```bash
sbt run

# or, if you want to watch the source code changes
 
sbt ~reStart
``` 

you can run queries interactively using [graphql-playground](https://github.com/prisma/graphql-playground) 
by opening [http://localhost:4040](http://localhost:4040) in a browser or query the `/graphql` endpoint directly. 
The HTTP endpoint follows [GraphQL best practices for handling the HTTP requests](http://graphql.org/learn/serving-over-http/#http-methods-headers-and-body).

Here are some examples of the queries you can make:

```bash
$ curl -X GET 'http://localhost:4040/entity' -H 'Accept-Encoding: gzip, deflate, br' \ 
-H 'Content-Type: application/json' -H 'Accept: application/json' -H 'Connection: keep-alive' \
-H 'DNT: 1' -H 'Origin: http://localhost:4040'  --compressed | jq
```
this gives back the json response (all the entities in test/resources/entitymodels)

```json
{
  "data": {
    "Entities": {
      "names": [
        "Admissions.entity.json",
        "Diagnoses.entity.json",
        "Labs.entity.json",
        "Patients.entity.json",
        "SchemaCompleteEntityType.json",
        "es-customer-pii.json",
        "person-data.json",
        "race-data.json",
        "supplier-data.json",
        "test.json"
      ]
    }
  }
}
```

```bash
$ curl 'http://localhost:4040/entity' -H 'Accept-Encoding: gzip, deflate, br' \
-H 'Content-Type: application/json' -H 'Accept: application/json' -H 'Connection: keep-alive' \
-H 'DNT: 1' -H 'Origin: http://localhost:4040' 
--data-binary '{"query":"query TestGetEntityByName($fileName: String!, $entityName: String!) {\n  
EntityDefinitionByName(fileName: $fileName, entityName: $entityName) {\n      name\n      description\n      id\n      
primaryKey\n      required\n      rangeIndex\n      pathRangeIndex\n      elementRangeIndex\n      wordLexicon\n      
namespace\n      namespacePrefix\n      properties {\n        name\n        type\n        ref\n        
collation\n        description\n        item {\n          type\n          refParent\n          
ref\n          collation\n  	}\n      }\n  }\n }\n",
"variables":{"fileName":"patient/Patients.entity.json"}}' --compressed | jq
```
this gives back the json response (passing one variable)

```json
{
  "data": {
    "EntityModel": {
      "name": "Patients.entity",
      "definitions": [
        {
          "name": "Labs"
        },
        {
          "name": "Admissions"
        },
        {
          "name": "Patients"
        },
        {
          "name": "Diagnoses"
        }
      ]
    }
  }
}
```

```bash
$ curl 'http://localhost:4040/entity' -H 'Accept-Encoding: gzip, deflate, br' \
-H 'Content-Type: application/json' -H 'Accept: application/json' -H 'Connection: keep-alive' \
-H 'DNT: 1' -H 'Origin: http://localhost:4040' 
--data-binary '{"query":"query TestGetEntityByName($fileName: String!, $entityName: String!) {\n  
EntityDefinitionByName(fileName: $fileName, entityName: $entityName) {\n      name\n      description\n      id\n      
primaryKey\n      required\n      rangeIndex\n      pathRangeIndex\n      elementRangeIndex\n      wordLexicon\n      
namespace\n      namespacePrefix\n      properties {\n        name\n        type\n        ref\n        
collation\n        description\n        item {\n          type\n          refParent\n          
ref\n          collation\n  	}\n      }\n  }\n }\n",
"variables":{"fileName":"patient/Patients.entity.json","entityName":"Patients"}}' --compressed | jq
```

this gives back the json response (passing two variables)

```json
{
  "data": {
    "EntityDefinitionByName": {
      "name": "Patients",
      "description": null,
      "id": null,
      "primaryKey": null,
      "required": null,
      "rangeIndex": null,
      "pathRangeIndex": null,
      "elementRangeIndex": [
        "PatientID"
      ],
      "wordLexicon": null,
      "namespace": null,
      "namespacePrefix": null,
      "properties": [
        {
          "name": "PatientID",
          "type": "string",
          "ref": null,
          "collation": "http://marklogic.com/collation/codepoint",
          "description": null,
          "item": null
        },
        {
          "name": "gender",
          "type": "string",
          "ref": null,
          "collation": "http://marklogic.com/collation/codepoint",
          "description": null,
          "item": null
        },
        {
          "name": "dob",
          "type": "string",
          "ref": null,
          "collation": "http://marklogic.com/collation/codepoint",
          "description": null,
          "item": null
        },
        {
          "name": "race",
          "type": "string",
          "ref": null,
          "collation": "http://marklogic.com/collation/codepoint",
          "description": null,
          "item": null
        },
        {
          "name": "marital-status",
          "type": "string",
          "ref": null,
          "collation": "http://marklogic.com/collation/codepoint",
          "description": null,
          "item": null
        },
        {
          "name": "language",
          "type": "string",
          "ref": null,
          "collation": "http://marklogic.com/collation/codepoint",
          "description": null,
          "item": null
        },
        {
          "name": "percentagebelowpoverty",
          "type": "decimal",
          "ref": null,
          "collation": null,
          "description": null,
          "item": null
        },
        {
          "name": "admissions",
          "type": "array",
          "ref": null,
          "collation": null,
          "description": null,
          "item": {
            "type": "object",
            "refParent": null,
            "ref": "#/definitions/Admissions",
            "collation": null
          }
        }
      ]
    }
  }
}
```

Here is another example to return default query result:

```bash
$ curl 'http://localhost:4040/entity' -H 'Accept-Encoding: gzip, deflate, br' \
-H 'Content-Type: application/json' -H 'Accept: application/json' -H 'Connection: keep-alive' -H 'DNT: 1' \
-H 'Origin: http://localhost:4040' --data-binary '{"variables":{"fileName":"patient/Patients.entity.json"}}' --compressed | jq
```

The result should be something like this:

```json
{
  "data": {
    "EntityModel": {
      "name": "Patients.entity",
      "version": "0.0.1",
      "id": null,
      "title": "Patients",
      "description": "Patient Model",
      "triples": null,
      "definitions": [
        {
          "name": "Labs",
          "description": null,
          "id": null,
          "primaryKey": null,
          "required": null,
          "rangeIndex": null,
          "pathRangeIndex": null,
          "elementRangeIndex": null,
          "wordLexicon": null,
          "namespace": null,
          "namespacePrefix": null,
          "properties": [
            {
              "name": "name",
              "type": "string",
              "ref": null,
              "collation": "http://marklogic.com/collation/codepoint",
              "description": null,
              "item": null
            },
            {
              "name": "value",
              "type": "string",
              "ref": null,
              "collation": "http://marklogic.com/collation/codepoint",
              "description": null,
              "item": null
            },
            {
              "name": "units",
              "type": "string",
              "ref": null,
              "collation": "http://marklogic.com/collation/codepoint",
              "description": null,
              "item": null
            },
            {
              "name": "datetime",
              "type": "string",
              "ref": null,
              "collation": "http://marklogic.com/collation/codepoint",
              "description": null,
              "item": null
            }
          ]
        },
        {
          "name": "Admissions",
          "description": null,
          "id": null,
          "primaryKey": null,
          "required": null,
          "rangeIndex": null,
          "pathRangeIndex": null,
          "elementRangeIndex": [
            "AdmissionID"
          ],
          "wordLexicon": null,
          "namespace": null,
          "namespacePrefix": null,
          "properties": [
            {
              "name": "AdmissionID",
              "type": "string",
              "ref": null,
              "collation": "http://marklogic.com/collation/codepoint",
              "description": null,
              "item": null
            },
            {
              "name": "startdate",
              "type": "string",
              "ref": null,
              "collation": "http://marklogic.com/collation/codepoint",
              "description": null,
              "item": null
            },
            {
              "name": "enddate",
              "type": "string",
              "ref": null,
              "collation": "http://marklogic.com/collation/codepoint",
              "description": null,
              "item": null
            },
            {
              "name": "labs",
              "type": "array",
              "ref": null,
              "collation": null,
              "description": null,
              "item": {
                "type": "object",
                "refParent": null,
                "ref": "#/definitions/Labs",
                "collation": null
              }
            },
            {
              "name": "diagnoses",
              "type": "array",
              "ref": null,
              "collation": null,
              "description": null,
              "item": {
                "type": "object",
                "refParent": null,
                "ref": "#/definitions/Diagnoses",
                "collation": null
              }
            }
          ]
        },
        {
          "name": "Patients",
          "description": null,
          "id": null,
          "primaryKey": null,
          "required": null,
          "rangeIndex": null,
          "pathRangeIndex": null,
          "elementRangeIndex": [
            "PatientID"
          ],
          "wordLexicon": null,
          "namespace": null,
          "namespacePrefix": null,
          "properties": [
            {
              "name": "PatientID",
              "type": "string",
              "ref": null,
              "collation": "http://marklogic.com/collation/codepoint",
              "description": null,
              "item": null
            },
            {
              "name": "gender",
              "type": "string",
              "ref": null,
              "collation": "http://marklogic.com/collation/codepoint",
              "description": null,
              "item": null
            },
            {
              "name": "dob",
              "type": "string",
              "ref": null,
              "collation": "http://marklogic.com/collation/codepoint",
              "description": null,
              "item": null
            },
            {
              "name": "race",
              "type": "string",
              "ref": null,
              "collation": "http://marklogic.com/collation/codepoint",
              "description": null,
              "item": null
            },
            {
              "name": "marital-status",
              "type": "string",
              "ref": null,
              "collation": "http://marklogic.com/collation/codepoint",
              "description": null,
              "item": null
            },
            {
              "name": "language",
              "type": "string",
              "ref": null,
              "collation": "http://marklogic.com/collation/codepoint",
              "description": null,
              "item": null
            },
            {
              "name": "percentagebelowpoverty",
              "type": "decimal",
              "ref": null,
              "collation": null,
              "description": null,
              "item": null
            },
            {
              "name": "admissions",
              "type": "array",
              "ref": null,
              "collation": null,
              "description": null,
              "item": {
                "type": "object",
                "refParent": null,
                "ref": "#/definitions/Admissions",
                "collation": null
              }
            }
          ]
        },
        {
          "name": "Diagnoses",
          "description": null,
          "id": null,
          "primaryKey": null,
          "required": null,
          "rangeIndex": null,
          "pathRangeIndex": null,
          "elementRangeIndex": null,
          "wordLexicon": null,
          "namespace": null,
          "namespacePrefix": null,
          "properties": [
            {
              "name": "primaryDiagnosisCode",
              "type": "string",
              "ref": null,
              "collation": "http://marklogic.com/collation/codepoint",
              "description": null,
              "item": null
            },
            {
              "name": "primaryDiagnosisDescription",
              "type": "string",
              "ref": null,
              "collation": "http://marklogic.com/collation/codepoint",
              "description": null,
              "item": null
            }
          ]
        }
      ]
    }
  }
}
```
