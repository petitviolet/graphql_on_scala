import sbt.Keys._

val libVersion = "1.0"

val scala = "2.12.6"

val loggerDependencies = Seq(
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "ch.qos.logback" % "logback-core" % "1.2.3"
)

val commonDependencies = {
  val AKKA_HTTP = "10.1.5"
  Seq(
    "net.petitviolet" %% "operator" % "0.4.0",
    "org.sangria-graphql" %% "sangria" % "1.4.2",
    "org.sangria-graphql" %% "sangria-slowlog" % "0.1.8",
    "org.sangria-graphql" %% "sangria-spray-json" % "1.0.1",
//  "org.sangria-graphql" %% "sangria-circe" % "1.1.1",
    "com.typesafe.akka" %% "akka-http" % AKKA_HTTP,
    "com.typesafe.akka" %% "akka-http-spray-json" % AKKA_HTTP,
    "com.typesafe.akka" %% "akka-stream" % "2.5.16",
    "com.lihaoyi" %% "sourcecode" % "0.1.4",
    "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  ) ++ loggerDependencies
}

def commonSettings(_name: String) = Seq(
  scalaVersion := scala,
  version := libVersion,
  libraryDependencies ++= commonDependencies,
  name := _name,
  trapExit := false,
  scalafmtConfig := Some(file(".scalafmt.conf")),
  scalafmtOnCompile := true,
)

lazy val graphqlOnScala = (project in file("."))
  .settings(commonSettings("graphqlOnScala"))
  .aggregate(main)

lazy val main = (project in file("modules/main"))
  .enablePlugins(JavaServerAppPackaging)
  .settings(commonSettings("main"))
  .settings(
    packageName in Docker := "graphql_on_scala",
    version in Docker := libVersion,
    dockerRepository := Some("petitviolet"),
    maintainer in Docker := "petitviolet <mail@petitviolet.net>",
    dockerExposedPorts := List(8080),
    dockerBaseImage := "openjdk:8-jre-slim",
    dockerCmd := Nil
  )

