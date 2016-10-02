name := """jugreactive"""

version := "1.0"

scalaVersion := "2.11.8"

lazy val akkaVersion = "2.4.2"

val akka="com.typesafe.akka" %% "akka-actor" % akkaVersion
val akkaTest="com.typesafe.akka" %% "akka-testkit" % akkaVersion
val akkaStream="com.typesafe.akka" %% "akka-stream" % "2.4.10"
val akkaStreamsTest="com.typesafe.akka" %% "akka-stream-testkit" % "2.4.10" % "test"


libraryDependencies ++= Seq(akka,akkaTest,akkaStream,akkaStreamsTest)

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % "test"
