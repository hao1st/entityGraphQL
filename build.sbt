name := "entity graphQL"
version := "0.1.0-SNAPSHOT"

description := "An example GraphQL server written with akka-http, circe and sangria."

scalaVersion := "2.12.8"
lazy val jacksonVersion = "2.9.8"
scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "com.marklogic" % "marklogic-client-api" % "5.0.0", // exclude("com.google", "guava"),
  "javax.ws.rs" % "javax.ws.rs-api" % "2.1" artifacts( Artifact("javax.ws.rs-api", "jar", "jar")),
  
  "org.sangria-graphql" %% "sangria" % "1.4.2",
  "org.sangria-graphql" %% "sangria-slowlog" % "0.1.8",
  "org.sangria-graphql" %% "sangria-circe" % "1.2.1",

  "com.typesafe.akka" %% "akka-http" % "10.1.3",
  "de.heikoseeberger" %% "akka-http-circe" % "1.21.0",

  //"com.fasterxml.jackson.core" % "jackson-core" % "2.9.5",
  //"com.fasterxml.jackson.core" % "jackson-databind" % "2.9.5",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.8",
  // https://mvnrepository.com/artifact/com.fasterxml.jackson.dataformat/jackson-dataformat-cbor
  //"com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor" % "2.9.5",

  "io.circe" %%	"circe-core" % "0.9.3",
  "io.circe" %% "circe-parser" % "0.9.3",
  "io.circe" %% "circe-optics" % "0.9.3",

  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  //"ch.qos.logback" % "logback-classic" % "1.2.3" % Test,
)
